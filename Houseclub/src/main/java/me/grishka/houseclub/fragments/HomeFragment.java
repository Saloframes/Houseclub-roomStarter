package me.grishka.houseclub.fragments;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.stream.Collectors;

import me.grishka.appkit.Nav;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.fragments.BaseRecyclerFragment;
import me.grishka.appkit.imageloader.ImageLoaderRecyclerAdapter;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.ViewImageLoader;
import me.grishka.appkit.utils.BindableViewHolder;
import me.grishka.appkit.utils.V;
import me.grishka.houseclub.MainActivity;
import me.grishka.houseclub.R;
import me.grishka.houseclub.VoiceService;
import me.grishka.houseclub.api.ClubhouseSession;
import me.grishka.houseclub.api.methods.GetChannels;
import me.grishka.houseclub.api.methods.GetProfile;
import me.grishka.houseclub.api.model.Channel;
import me.grishka.houseclub.api.model.ChannelUser;
import me.grishka.houseclub.api.model.FullUser;

public class HomeFragment extends BaseRecyclerFragment<Channel> implements VoiceService.ChannelEventListener{

	private Button leave, create_room;
	private ImageView ich_pic1, ich_pic2, ich_pict;
	private TextView ich_all;
	private LinearLayout current_room;
	private ChannelAdapter adapter;
	private ViewOutlineProvider roundedCornersOutline=new ViewOutlineProvider(){
		@Override
		public void getOutline(View view, Outline outline){
			outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), V.dp(28));
		}
	};
	private ViewOutlineProvider roundedTopCornersOutline=new ViewOutlineProvider(){
		@Override
		public void getOutline(View view, Outline outline){
			float cornerRadius = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, 32f, getResources().getDisplayMetrics());
			outline.setRoundRect(0, 0, view.getWidth(), (int)(view.getHeight() + cornerRadius), cornerRadius);
		}
	};
	private Drawable placeholder;

	public HomeFragment(){
		super(20);
		setListLayoutId(R.layout.home);
	}

	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		loadData();
		setHasOptionsMenu(true);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetChannels()
				.setCallback(new SimpleCallback<GetChannels.Response>(this){
					@Override
					public void onSuccess(GetChannels.Response result){
						currentRequest=null;
						onDataLoaded(result.channels, false);
						bindCurrentRoomView();
					}
				}).exec();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState){
		super.onViewCreated(view, savedInstanceState);

		leave=view.findViewById(R.id.leave);
		create_room=view.findViewById(R.id.create_room);

		leave.setOnClickListener(this::onLeaveClick);
		create_room.setOnClickListener(this::onCreateRoomClick);

		current_room=view.findViewById(R.id.current_room);
		current_room.setOutlineProvider(roundedTopCornersOutline);
		current_room.setClipToOutline(true);
		ich_pic1=view.findViewById(R.id.ich_pic1);
		ich_pic2=view.findViewById(R.id.ich_pic2);
		ich_pict=view.findViewById(R.id.ich_pict);
		ich_all=view.findViewById(R.id.ich_all);
		current_room.setVisibility(View.GONE);

		placeholder=new ColorDrawable(getResources().getColor(R.color.grey));
		ich_pict.setImageDrawable(placeholder);
		list.addItemDecoration(new RecyclerView.ItemDecoration(){
			@Override
			public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state){
				outRect.bottom=outRect.top=V.dp(8);
				outRect.left=outRect.right=V.dp(16);
			}
		});
		VoiceService.addListener(this);

		getToolbar().setElevation(0);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		getToolbar().setElevation(0);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		if(adapter==null){
			adapter=new ChannelAdapter();
			adapter.setHasStableIds(true);
		}
		return adapter;
	}

	@Override
	public boolean wantsLightNavigationBar(){
		return true;
	}

	@Override
	public boolean wantsLightStatusBar(){
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
//		menu.add(0,0,0,"").setIcon(R.drawable.ic_notifications).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//		menu.add(0,1,0,"").setIcon(R.drawable.ic_baseline_person_24).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		inflater.inflate(R.menu.menu_home, menu);

		FrameLayout frame = (FrameLayout) menu.findItem(R.id.homeMenuProfile).getActionView();
		frame.setOutlineProvider(roundedCornersOutline);
		frame.setClipToOutline(true);
		frame.setOnClickListener((v) -> {
			Bundle args=new Bundle();
			args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
			Nav.go(getActivity(), ProfileFragment.class, args);
		});

		ImageView pro_pic = frame.findViewById(R.id.pro_pic);
		new GetProfile(Integer.parseInt(ClubhouseSession.userID))
				.setCallback(new SimpleCallback<GetProfile.Response>(this) {
					@Override
					public void onSuccess(GetProfile.Response result) {
						FullUser user=result.userProfile;
						ColorDrawable d=new ColorDrawable(getResources().getColor(R.color.grey));
						if(user.photoUrl!=null)
							ViewImageLoader.load(pro_pic, d, user.photoUrl);
						else
							pro_pic.setImageDrawable(d);
					}

					@Override
					public void onError(ErrorResponse error) {
						error.showToast(getActivity());
					}
				}).exec();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.homeMenuSearchPeople) {
			Bundle args = new Bundle();
			Nav.go(getActivity(), SearchListFragment.class, args);
			return true;
		}
		else if (item.getItemId() == R.id.homeMenuNotifications) {
			Bundle args = new Bundle();
			args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
			Nav.go(getActivity(), NotificationListFragment.class, args);
			return true;
		}
		else if (item.getItemId() == R.id.homeMenuEvents) {
			Bundle args = new Bundle();
			args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
			Nav.go(getActivity(), EventsFragment.class, args);
			return true;
		}

		else if (item.getItemId() == R.id.homeMenuSearchClubs) {
			Bundle args = new Bundle();
			args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
			Nav.go(getActivity(), SearchClubsListFragment.class, args);
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	private void bindCurrentRoomView() {
		VoiceService svc = VoiceService.getInstance();
		if(svc == null) {
			V.setVisibilityAnimated(current_room, View.GONE);
			return;
		}

		list.post(() -> {
			for (int i = 0; i < list.getChildCount(); i++) {
				RecyclerView.ViewHolder h = list.findViewHolderForAdapterPosition(i);
				if(h instanceof ChannelViewHolder && svc.getChannel().channelId == ((ChannelViewHolder) h).getItem().channelId) {
					ich_pic2.setVisibility(View.GONE);
					ich_pict.setVisibility(View.GONE);

					ich_pic1.setImageDrawable(((ChannelViewHolder) h).pic1.getDrawable());
					if(!((ChannelViewHolder) h).pic2.getDrawable().equals(placeholder)) {
						ich_pic2.setImageDrawable(((ChannelViewHolder) h).pic2.getDrawable());
						ich_pic2.setVisibility(View.VISIBLE);
						if(svc.getChannel().users.size() > 2) {
							ich_all.setText("+" + (svc.getChannel().users.size() - 2));
							ich_pict.setVisibility(View.VISIBLE);
						}
					}

					current_room.setOnClickListener(v -> ((MainActivity)getActivity()).joinChannel(svc.getChannel().channel));

					V.setVisibilityAnimated(current_room, View.VISIBLE);
				}
			}
		});
	}

	private void onCreateRoomClick(View view) {
		Bundle args=new Bundle();
		args.putInt("id", Integer.parseInt(ClubhouseSession.userID));
		Nav.go(getActivity(), CreateChannelFragment.class, args);
	}

	private void onLeaveClick(View v){
		VoiceService.getInstance().leaveCurrentChannel();
	}

	@Override
	public void onChannelUpdated(Channel channel) {
		refresh();
	}

	@Override
	public void onChannelEnded() {
		refresh();
	}

	private class ChannelAdapter extends RecyclerView.Adapter<ChannelViewHolder> implements ImageLoaderRecyclerAdapter{

		@NonNull
		@Override
		public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
			return new ChannelViewHolder();
		}

		@Override
		public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position){
			holder.bind(data.get(position));
		}

		@Override
		public int getItemCount(){
			return data.size();
		}

		@Override
		public long getItemId(int position){
			return data.get(position).channelId;
		}

		@Override
		public int getImageCountForItem(int position){
			Channel chan=data.get(position);
			int count=0;
			for(int i=0;i<Math.min(2, chan.users.size());i++){
				if(chan.users.get(i).photoUrl!=null)
					count++;
			}
			return count;
		}

		@Override
		public String getImageURL(int position, int image){
			Channel chan=data.get(position);
			for(int i=0;i<Math.min(2, chan.users.size());i++){
				if(chan.users.get(i).photoUrl!=null){
					if(image==0)
						return chan.users.get(i).photoUrl;
					else
						image--;
				}
			}
			return null;
		}
	}

	private class ChannelViewHolder extends BindableViewHolder<Channel> implements View.OnClickListener, ImageLoaderViewHolder{

		private TextView topic, club, speakers, numMembers, numSpeakers;
		private ImageView pic1, pic2;

		public ChannelViewHolder(){
			super(getActivity(), R.layout.channel_row);
			topic=findViewById(R.id.topic);
			club=findViewById(R.id.club);
			speakers=findViewById(R.id.speakers);
			numSpeakers=findViewById(R.id.num_speakers);
			numMembers=findViewById(R.id.num_members);
			pic1=findViewById(R.id.pic1);
			pic2=findViewById(R.id.pic2);

			itemView.setOutlineProvider(roundedCornersOutline);
			itemView.setClipToOutline(true);
			/*itemView.setElevation(V.dp(2));*/
			itemView.setOnClickListener(this);
		}

		@Override
		public void onBind(Channel item){
			numMembers.setText(""+item.numAll);
			numSpeakers.setText(""+item.numSpeakers);
			speakers.setText(item.users.stream().map(user->user.isSpeaker ? (user.name+" 💬") : user.name).collect(Collectors.joining("\n")) );

			imgLoader.bindViewHolder(adapter, this, getAdapterPosition());

			pic1.setImageDrawable(placeholder);
			pic2.setImageDrawable(placeholder);

			topic.setVisibility(View.GONE);
			if(item.topic != null) {
				topic.setText(item.topic);
				topic.setVisibility(View.VISIBLE);
			}

			club.setVisibility(View.GONE);
			if(item.club_name != null) {
				club.setText(item.club_name);
				club.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onClick(View view){
			((MainActivity)getActivity()).joinChannel(item.channel);
		}

		private ImageView imgForIndex(int index){
			if(index==0)
				return pic1;
			return pic2;
		}

		@Override
		public void setImage(int index, Bitmap bitmap){
			if(index==0 && item.users.get(0).photoUrl==null)
				index=1;
			imgForIndex(index).setImageBitmap(bitmap);
		}

		@Override
		public void clearImage(int index){
			imgForIndex(index).setImageDrawable(placeholder);
		}
	}
}
