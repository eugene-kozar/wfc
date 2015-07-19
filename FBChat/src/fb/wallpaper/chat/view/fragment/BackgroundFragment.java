package fb.wallpaper.chat.view.fragment;

import org.apache.log4j.Logger;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

import fb.wallpaper.chat.R;
import fb.wallpaper.chat.utils.Constants;
import fb.wallpaper.chat.view.adapters.ImageGalleryAdapter;
import fb.wallpaper.chat.view.list.adapter.ImagePagerAdapter;

public class BackgroundFragment extends TrackedFragment {

	private static final Logger LOG = Logger.getLogger(BackgroundFragment.class);
	private SharedPreferences sharedPref;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("Creating backgrounds fragment");
		View view = inflater.inflate(R.layout.fragment_background, container, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		
		final Gallery gallery = (Gallery) view.findViewById(R.id.gallery);
		final ViewPager pager = (ViewPager) view.findViewById(R.id.pager);	
		
		/*final CheckBox checkBox = (CheckBox) view.findViewById(R.id.home_screen);
		if(sharedPref.getBoolean("home_screen", true)) {
			checkBox.setChecked(true);
		} else {
			checkBox.setChecked(false);
		}
		checkBox.setOnClickListener(new View.OnClickListener() {
	          @Override
	          public void onClick(View v) { 
	              if(checkBox.isChecked()) {
	          		sharedPref.edit().putBoolean("home_screen", true).commit();
	              } else {
	          		sharedPref.edit().putBoolean("home_screen", false).commit();
	              }
	          }
	    });*/

		//final Spinner m_myDynamicSpinner = (Spinner) view.findViewById(R.id.picture_category);

		gallery.setAdapter(new ImageGalleryAdapter(getSherlockActivity(), Constants.Thor2_80x80));
		gallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				pager.setCurrentItem(position);
			}
		});
		String array[] = { "Thor 2" };
		ArrayAdapter<String> sp_adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, array);
		sp_adapter.setDropDownViewResource(R.layout.spinner_item);
		pager.setAdapter(new ImagePagerAdapter(getActivity(), Constants.Thor2));
		pager.setCurrentItem(2);
		gallery.setSelection(2);
		/*m_myDynamicSpinner.setAdapter(sp_adapter);
		m_myDynamicSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				String picture_category = m_myDynamicSpinner.getSelectedItem().toString();
				if("Thor 2".equals(picture_category)) {	
					pager.setAdapter(new ImagePagerAdapter(getActivity(), Constants.Thor2));
				}
				else if("Cars".equals(picture_category)) {
        			pager.setAdapter(new ImagePagerAdapter(getActivity(), Constants.IMAGES_CARS));
        			gallery.setAdapter(new ImageGalleryAdapter(getSherlockActivity(), Constants.IMAGES_CARS));
        		}
				pager.setCurrentItem(2);
				gallery.setSelection(2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {}

		});	*/	

		return view;
	}

}

