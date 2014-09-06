package com.dna.radius.businessmode;

import java.io.ByteArrayOutputStream;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
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
import android.widget.Button;
import android.widget.ImageView;

import com.dna.radius.R;

public class BusinessChooseImageFragment extends  Fragment{
	
	private final static int RESULT_LOAD_IMAGE_GALLERY = 1;
	private final static int RESULT_LOAD_IMAGE_CAMERA = 2;
	
	private static final int IMAGE_HEIGHT = 360;
	private static final int IMAGE_WIDTH = 480;
	
	private ImageView imageView;
	
	private Bitmap currentBitmap = null;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.business_choose_image_fragment,container, false);	
		
		Button cameraButton = (Button)view.findViewById(R.id.camera_button);
		Button galleryButton = (Button)view.findViewById(R.id.gallery_button);
		imageView = (ImageView)view.findViewById(R.id.business_choose_image_view);

		cameraButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
					startActivityForResult(takePictureIntent, RESULT_LOAD_IMAGE_CAMERA);
				}

			}
		});

		galleryButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(
						Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(i, RESULT_LOAD_IMAGE_GALLERY);				
			}
		});
		return view;
	}




	/**
	 * currently used for handling an image which was picked from the gallery by the user, or
	 * an image which was took by the camera.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Bitmap bMap = null;
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

			bMap = BitmapFactory.decodeFile(picturePath);

		}
		else if (requestCode == RESULT_LOAD_IMAGE_CAMERA  && resultCode == FragmentActivity.RESULT_OK) {
			Bundle extras = data.getExtras();
			bMap = (Bitmap) extras.get("data");

		}else{
			Log.e("BusinessChooseImageFragment", "returned from unknown activity");
			return;
		}

		if(bMap!=null){
			//TODO - if we want to resize the bMap, that's the way to do it
			//Bitmap resizedBitmap = Bitmap.createScaledBitmap(bMap, 480,320, false);
			bMap = processImage(bMap);
			imageView.setImageBitmap(bMap);
			currentBitmap = bMap;
		}else{
			Log.e("BusinessChooseImageFragment", "returned bitmap is null");
		}
	}
	
	/**
	 * return true iff the user filled all the relevant data
	 * @return
	 */
	public boolean didUserFillAllData() {
		return currentBitmap != null;
	}
	
	
	/**
	 * downsamples the image to size IMAGE_HEIGHTxIMAGE_WIDTH,
	 * also, performs jpeg comression.
	 * @return
	 */
	Bitmap processImage(Bitmap bitmap){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		//downsampling
		double proportion = (double)bitmap.getHeight() / (double)IMAGE_HEIGHT;
		int newImageHeight = IMAGE_HEIGHT;
		int newImageWidth= (int)((double)bitmap.getWidth() / proportion);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newImageWidth, newImageHeight, true);
		
		//cropps from the center
		Bitmap croppedBitmap = ThumbnailUtils.extractThumbnail(resizedBitmap, IMAGE_WIDTH, IMAGE_HEIGHT);
		
		//jpeg compression
		croppedBitmap.compress(CompressFormat.JPEG, 100, baos);
		
		Log.d("BusinessChooseImage","returned a new image, width,height=" +croppedBitmap.getWidth() + "," +  croppedBitmap.getHeight() );
		Log.d("BusinessChooseImage","original size: width,height=" +bitmap.getWidth() + "," +  bitmap.getHeight() );
		return croppedBitmap;
	}
	
	public Bitmap getImageBitmap(){
		return currentBitmap;
	}
	
	
	public boolean neededInfoGiven(){
		return currentBitmap != null;
	}




}
