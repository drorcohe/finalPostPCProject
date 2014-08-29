package com.dna.radius.businessmode;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dna.radius.datastructures.Comment;
import com.dna.radius.dbhandling.DBHandler;
import com.dna.radius.mapsample.CommentsArrayAdapter;
import com.example.mapsample.R;

/**
 * represents the first screen which the business owner sees when he is logging in.
 * contains data about his deal, an image, comments and top deals segment.
 *
 */
public class BusinessDashboardFragment extends Fragment{
	private DBHandler dbHandler;
	private BusinessOpeningScreenActivity  activityParent = null;
	private ArrayList<Comment> commentsList;
	private ImageView imageView;

	/**this variable is used for loading an image from the gallery*/
	private final static int RESULT_LOAD_IMAGE = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.business_dashboard_fragment,container, false);	

		activityParent = (BusinessOpeningScreenActivity)getActivity();

		dbHandler = new DBHandler();
		final TextView dealTv = (TextView) view.findViewById(R.id.deal_tv);
		dealTv.setText(activityParent.ownerData.currentDeal);
		
		/*handles the image of the business*/
		imageView = (ImageView)view.findViewById(R.id.buisness_image_view);
		if(activityParent.ownerData.hasImage){
			imageView.setImageBitmap(activityParent.ownerData.image);
		}else{
			imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.set_business_image));
		}
		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE);
			}
		});


		/**handles the comments segment*/
		commentsList = new ArrayList<>();
		ListView commentsListView = (ListView)view.findViewById(R.id.comments_list_view);
		CommentsArrayAdapter commentsAdapter = new CommentsArrayAdapter(activityParent,android.R.layout.simple_list_item_1 , commentsList);
		commentsListView.setAdapter(commentsAdapter);
		dbHandler.loadCommentsListAsync(commentsList, commentsAdapter);

		/**handles the number of likes and dislikes*/
		TextView numOfLikesTV = (TextView)view.findViewById(R.id.num_of_likes_tv);
		numOfLikesTV.setText(Long.toString(activityParent.ownerData.numberOfLikes));
		TextView numOfDislikesTV = (TextView)view.findViewById(R.id.num_of_dislikes_tv);
		numOfDislikesTV.setText(Long.toString(activityParent.ownerData.numberOfDislikes));

		/***
		 * allows adding a new deal instead of the old one
		 */
		ImageView addDeal = (ImageView)view.findViewById(R.id.add_deal_iv);
		addDeal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input = new EditText(activityParent);
				// TODO Auto-generated method stub
				new AlertDialog.Builder(activityParent)
				.setTitle("Add A new Deal")
				.setMessage("please add a new deal to replace the old one")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String newDealStr = input.getText().toString();
						activityParent.ownerData.changeDeal(newDealStr);
						dealTv.setText(newDealStr);
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Do nothing.
					}
				}).show();

			}
		});

		/**sets the top businesses segment*/
		TopBusinessesHorizontalView topBusinessesScroll = (TopBusinessesHorizontalView)view.findViewById(R.id.top_businesses_list_view);
		dbHandler.LoadTopBusinessesAsync(topBusinessesScroll,activityParent);

		return view;




	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if(dbHandler!=null){
			dbHandler.close();
			dbHandler = null;
		}
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if(dbHandler!=null){
			dbHandler.close();
			dbHandler = null;
		}
	}


	/**
	 * currently used for handling an image which was picked from the gallery by the user.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		/**receives an image from the gallery, and change the image of the business*/
		if (requestCode == RESULT_LOAD_IMAGE && resultCode == FragmentActivity.RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getActivity().getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();
			
			Bitmap newBmap = BitmapFactory.decodeFile(picturePath);
			activityParent.ownerData.changeBusinessImage(BitmapFactory.decodeFile(picturePath));
			// String picturePath contains the path of selected Image
			imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
		}
	}

}
