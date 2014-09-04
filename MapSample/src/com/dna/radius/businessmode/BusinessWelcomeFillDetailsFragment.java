package com.dna.radius.businessmode;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dna.radius.R;
import com.dna.radius.datastructures.ExternalBusiness.BuisnessType;

public class BusinessWelcomeFillDetailsFragment extends Fragment{

	private EditText businessNameEditText, businessAddressEditText, businessPhoneEditText;
	private int businessType;
	private Spinner businessTypeSpinner;
	private boolean typeSelected = false;
	private String businessName;
			
	
	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.business_welcome_fill_details_fragment,container, false);	

		businessNameEditText = (EditText) view.findViewById(R.id.business_name_textView);
		businessTypeSpinner = (Spinner) view.findViewById(R.id.business_type_spinner);
		businessPhoneEditText = (EditText) view.findViewById(R.id.business_phone_textView);
		businessAddressEditText = (EditText) view.findViewById(R.id.business_address_textView);
		
		setBusinessTypeSpinner();
		
		
		return view;
	}
	

	private void setBusinessTypeSpinner() {

		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
				R.array.business_type_spinner, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		businessTypeSpinner.setAdapter(adapter);

		businessTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view, 
					int pos, long id) {
				
				String typeStr = parent.getItemAtPosition(pos).toString();
				typeSelected = true;
				if (typeStr.compareToIgnoreCase(BuisnessType.RESTAURANT.getStringRep()) == 0){
					
					businessType = BuisnessType.RESTAURANT.getParseID();
					
				}
				else if (typeStr.compareToIgnoreCase(BuisnessType.PUB.getStringRep()) == 0){
					
					businessType = BuisnessType.PUB.getParseID();
				}
				else if (typeStr.compareToIgnoreCase(BuisnessType.COFFEE.getStringRep()) == 0){
					
					businessType = BuisnessType.COFFEE.getParseID();
				}
				else if (typeStr.compareToIgnoreCase(BuisnessType.GROCERIES.getStringRep()) == 0){
					
					businessType = BuisnessType.GROCERIES.getParseID();
				}
				else if (typeStr.compareToIgnoreCase(BuisnessType.ACCOMMODATION.getStringRep()) == 0){
					
					businessType = BuisnessType.ACCOMMODATION.getParseID();
				}else{
					Log.e("BusinessWelcomeFillDerailsFragment", "error choosing a spinner option");
					typeSelected = false;
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {
				typeSelected = false;
			}

		});

	}

	/**
	 * return true iff the user filled all the relevant data
	 * @return
	 */
	public boolean didUserFillAllData() {

		businessName = businessNameEditText.getText().toString();

		if (businessName.isEmpty() || illegalBusinessName(businessName)) {
			Toast.makeText(getActivity(), getResources().getString(R.string.business_name_forgot_to_fill), Toast.LENGTH_SHORT).show();
			return false;
		}
		if (!typeSelected) {
			Toast.makeText(getActivity(), getResources().getString(R.string.business_type_forgot_to_fill), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		return true;
		
	}
	
	private boolean illegalBusinessName(String bn) {

		//TODO business name filter? alpha-numeric only ? 
		return false;

	}
	
	public String getBusinessName(){
		return businessName;
	}
	public int getBusinessType(){
		return businessType;
	}
	public String getBusinessPhoneNumber(){
		return businessPhoneEditText.getText().toString();
	}
	public String getBusinessAddress(){
		return businessAddressEditText.getText().toString();
	}
}