package com.ypyproductions.musicplayer.object;

import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;



/**
 * 
 * 
 * @author:YPY Productions
 * @Skype: baopfiev_k50
 * @Mobile : +84 983 028 786
 * @Email: dotrungbao@gmail.com
 * @Website: www.ypyproductions.com
 * @Project:MusicPlayer
 * @Date:Dec 29, 2014
 * 
 */
public class PlaylistObject {
	private long id;
	private String name;
	private ArrayList<TrackObject> listTrackObjects;
	private ArrayList<Long> listTrackIds;

	public PlaylistObject(long id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.listTrackObjects = new ArrayList<TrackObject>();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<TrackObject> getListTrackObjects() {
		return listTrackObjects;
	}

	public void setListTrackObjects(ArrayList<TrackObject> listTrackObjects) {
		this.listTrackObjects = listTrackObjects;
	}

	public ArrayList<Long> getListTrackIds() {
		return listTrackIds;
	}

	public void setListTrackIds(ArrayList<Long> listTrackIds) {
		this.listTrackIds = listTrackIds;
	}
	
	public void addTrackObject(TrackObject mTrackObject,boolean isAddId){
		if(listTrackObjects!=null && mTrackObject!=null){
			listTrackObjects.add(mTrackObject);
			if(isAddId){
				listTrackIds.add(mTrackObject.getId());
			}
		}
	}
	
	public void removeTrackObject(TrackObject mTrackObject){
		if(listTrackObjects!=null && mTrackObject!=null){
			Iterator<TrackObject> mIterator = listTrackObjects.iterator();
			while (mIterator.hasNext()) {
				TrackObject trackObject = (TrackObject) mIterator.next();
				if(trackObject.getId()==mTrackObject.getId()){
					mIterator.remove();
					break;
				}
			}
			Iterator<Long> mTrackIdIterator = listTrackIds.iterator();
			while (mTrackIdIterator.hasNext()) {
				Long id = (Long) mTrackIdIterator.next();
				if(id.longValue()==mTrackObject.getId()){
					mTrackIdIterator.remove();
					break;
				}
			}
		}
	}
	
	public void removeTrackObject(long id){
		if(listTrackObjects!=null && listTrackObjects.size()>0){
			Iterator<TrackObject> mIterator = listTrackObjects.iterator();
			while (mIterator.hasNext()) {
				TrackObject trackObject = (TrackObject) mIterator.next();
				if (trackObject.getId() == id) {
					mIterator.remove();
					break;
				}
			}
		}
	}
	
	public TrackObject getTrackObject(long id){
		if(listTrackObjects!=null && listTrackObjects.size()>0){
			for(TrackObject mTrackObject:listTrackObjects){
				if(mTrackObject.getId()==id){
					return mTrackObject;
				}
			}
		}
		return null;
	}
	public boolean isSongAlreadyExited(long id){
		if(listTrackIds!=null && listTrackIds.size()>0){
			for(Long mId:listTrackIds){
				if(mId==id){
					return true;
				}
			}
		}
		return false;
	}
	public JSONObject toJson() {
		try {
			JSONObject mJsonObject = new JSONObject();
			mJsonObject.put("id", id);
			mJsonObject.put("name", name);

			JSONArray mJsonArray = new JSONArray();
			if (listTrackObjects != null && listTrackObjects.size() > 0) {
				for (TrackObject mObject : listTrackObjects) {
					mJsonArray.put(mObject.getId());
				}
			}
			mJsonObject.put("tracks", mJsonArray);
			return mJsonObject;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
