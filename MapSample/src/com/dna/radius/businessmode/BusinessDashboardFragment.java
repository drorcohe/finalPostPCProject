package com.dna.radius.businessmode;


import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dna.radius.R;
import com.dna.radius.datastructures.Comment;
import com.dna.radius.datastructures.ExternalBusiness;
import com.dna.radius.dbhandling.DBHandler;
import com.dna.radius.mapsample.CommentsArrayAdapter;

/**
 * represents the first screen which the business owner sees when he is logging in.
 * contains data about his deal, an image, comments and top deals segment.
 *
 */
public class BusinessDashboardFragment extends Fragment{
	private BusinessOpeningScreenActivity  activityParent = null;
	private ArrayList<Comment> commentsList;
	private ImageView imageView;

	/**this variable is used for loading an image from the gallery*/
	private final static int RESULT_LOAD_IMAGE_GALLERY = 1;
	private final static int RESULT_LOAD_IMAGE_CAMERA = 2;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.business_dashboard_fragment,container, false);	

		activityParent = (BusinessOpeningScreenActivity)getActivity();

		final TextView dealTv = (TextView) view.findViewById(R.id.deal_tv);

		if(BusinessData.hasADealOnDisplay()){
			dealTv.setText(BusinessData.currentDeal.getContent());
		}
		/*handles the image of the business*/
		imageView = (ImageView)view.findViewById(R.id.buisness_image_view);
		if(BusinessData.hasImage()){
			if(BusinessData.imageFullyLoaded()){
				imageView.setImageBitmap(BusinessData.businessImage);
				imageView.setVisibility(View.VISIBLE);
				//TODO - DROR handle imageview and progress bar visibility
			}else{
				BusinessData.loadImage(imageView, new ProgressBar(getActivity()));//TODO
			}
		}else{
			imageView.setVisibility(View.VISIBLE);
			imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.set_business_image));
		}

		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new AlertDialog.Builder(activityParent)
				.setTitle("Choose an image for your business")
				.setMessage("please choose an image source")
				.setPositiveButton("Camera", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
							startActivityForResult(takePictureIntent, RESULT_LOAD_IMAGE_CAMERA);
						}
					}
				}).setNegativeButton("Gallery", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent i = new Intent(
								Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(i, RESULT_LOAD_IMAGE_GALLERY);
					}
				}).show();


			}
		});


		if(BusinessData.hasADealOnDisplay()){
			/**handles the comments segment*/
			commentsList = new ArrayList<>();
			ListView commentsListView = (ListView)view.findViewById(R.id.comments_list_view);
			CommentsArrayAdapter commentsAdapter = new CommentsArrayAdapter(activityParent,android.R.layout.simple_list_item_1 , commentsList);
			commentsListView.setAdapter(commentsAdapter);
			DBHandler.loadCommentsListAsync(commentsList, commentsAdapter);
		}
		
		/**handles the number of likes and dislikes*/
		TextView numOfLikesTV = (TextView)view.findViewById(R.id.num_of_likes_tv);
		TextView numOfDislikesTV = (TextView)view.findViewById(R.id.num_of_dislikes_tv);
		if(BusinessData.hasADealOnDisplay()){
			numOfLikesTV.setText(Long.toString(BusinessData.currentDeal.getNumOfLikes()));
			numOfDislikesTV.setText(Long.toString(BusinessData.currentDeal.getNumOfDislikes()));
		}
		/***
		 * allows adding a new deal instead of the old one
		 */
		ImageView addDeal = (ImageView)view.findViewById(R.id.add_deal_iv);
		addDeal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final EditText input = new EditText(activityParent);
				new AlertDialog.Builder(activityParent)
				.setTitle("Add A new Deal")
				.setMessage("please add a new deal to replace the old one")
				.setView(input)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//TODO implement this method in businessData' saves the current deal into deal history
						//DBHandler.addDealToHistory(BusinessData.currentUser.getObjectId(),data.currentDeal,data.numberOfLikes,data.numberOfDislikes);

						//adds the new Deal
						String newDealStr = input.getText().toString();
						BusinessData.createNewDeal(newDealStr);
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
		for(ExternalBusiness b : activityParent.topBusinesses){
			topBusinessesScroll.addBusiness(b, null);
		}

		return view;




	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		DBHandler.close();
	}


	/**
	 * currently used for handling an image which was picked from the gallery by the user, or
	 * an image which was took by the camera.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap newBmap = null;
		/**receives an image from the gallery, and change the image of the business*/
		if (requestCode == RESULT_LOAD_IMAGE_GALLERY   && resultCode == FragmentActivity.RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getActivity().getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			newBmap = BitmapFactory.decodeFile(picturePath);

		}
		else if (requestCode == RESULT_LOAD_IMAGE_CAMERA  && resultCode == FragmentActivity.RESULT_OK) {
			Bundle extras = data.getExtras();
			newBmap = (Bitmap) extras.get("data");
		}else{
			return;
		}

		if(newBmap!=null){
			//BusinessData.setImage(newBmap.compress(CompressFormat.JPEG, )); TODO CANGE
			imageView.setImageBitmap(newBmap);
		}else{
			Log.e("BusinessDashboardFragment", "ERROR!! The RETURNED BITMAP IS NULL");
		}
	}

}
