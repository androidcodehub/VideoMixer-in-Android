package com.androidcodehub.demo.po;

import com.androidcodehub.demo.utils.ConvertToUtils;
import com.yixia.camera.util.FileUtils;
import com.yixia.camera.util.StringUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("serial")
public class POThemeSingle implements Serializable {

	public static final int THEME_TYPE_MUSIC = 3;

	public static final int THEME_TYPE_WATERMARK = 4;

	public static final int THEME_TYPE_MV = 5;

	public static final int THEME_TYPE_TEXT = 6;

	public static final int THEME_TYPE_FILTER = 11;

	public static final int THEME_TYPE_RECOMMEND_MV = 99;

	public static final int THEME_TYPE_RECOMMEND_WATERMARK = 98;

	public static final String THEME_STONE = "Stone";

	public static final String THEME_EMPTY = "Empty";

	public static final int SOUND_TYPE_LOCAL = 1;

	public static final int SOUND_TYPE_ONLINE = 2;

	public transient long _id;

	public String themeIcon;

	public int themeIconResource;

	public String themeDisplayName;

	public String themeName;

	public String themeDownloadUrl;

	public String themeUrl;

	public String themeFolder;

	public long themeUpdateAt;

	public boolean isLock;

	public boolean isBuy;

	public String sthid;

	public int pic_type;
	public String banner;
	public String channel_pic;

	public String desc;

	public int price;

	public String previewVideoPath;

	public String category;

	public String categoryBackup;

	public boolean isEmpty;

	public int themeType;
	public int lockType;


	public String musicName;

	public String musicTitle;

	public String musicPath;


	public String video;

	public float volumn = -1;

	public int soundType;

	public int soundPitch;

	public int soundEngine;

	public int soundVoicer;

	public boolean isMv;

	public boolean isMP4;
	public String watermarkBlendmode;
	public boolean isFilter;

	public boolean isSpeed;
	public float speed;

	public int index;

	public int position;

	public int itemType;

	public String message;

	public String fileExt;

	public int textSize;

	public boolean textBold;

	public String textGravity;

	public int textX;

	public int textY;

	public String textBackground;

	public String textColor;

	public boolean isCity;

	public boolean isCityPinyin;

	public boolean isWeather;

	public int status = -1;

	public int percent;

	public List<POThemeSingle> items = new ArrayList<POThemeSingle>();

	public POThemeSingle() {

	}

	public POThemeSingle(JSONObject jst, int themeType) {
		sthid = jst.optString("sthid");
		themeIcon = jst.optString("icon");
		banner = jst.optString("banner");
		channel_pic = jst.optString("channel_pic");
		themeDisplayName = jst.optString("name");
		themeName = jst.optString("folder_name").replace(" ", "_");
		desc = jst.optString("desc");
		price = ConvertToUtils.toInt(jst.optString("price"));
		previewVideoPath = jst.optString("channel");
		themeDownloadUrl = jst.optString("downurl");
		themeUpdateAt = ConvertToUtils.toLong(jst.optString("update_at"));
		isLock = jst.optBoolean("is_lock");
		isBuy = jst.optBoolean("is_buy");
		pic_type = jst.optInt("pic_type");
		this.themeType = themeType;
		this.lockType = jst.optInt("lock_type");
		isMv = themeType == THEME_TYPE_RECOMMEND_MV || themeType == THEME_TYPE_MV;
		if (themeType == THEME_TYPE_WATERMARK || themeType == THEME_TYPE_RECOMMEND_WATERMARK) {
			isMP4 = true;
		}
	}

	public void update(POThemeSingle theme) {
		if (theme == null) {
			return;
		}
		this.themeName = theme.themeName;
		this.themeDisplayName = theme.themeDisplayName;
		this.fileExt = theme.fileExt;
		this.isMv = theme.isMv;
		this.musicTitle = theme.musicTitle;
		//		this.category = theme.category;
		this.isMP4 = theme.isMP4;
		this.watermarkBlendmode = theme.watermarkBlendmode;
		this.textSize = theme.textSize;
		this.textBold = theme.textBold;
		this.textGravity = theme.textGravity;
		this.textBold = theme.textBold;
		this.textGravity = theme.textGravity;
		this.textX = theme.textX;
		this.textY = theme.textY;
		this.textBackground = theme.textBackground;
		this.textColor = theme.textColor;
		this.message = theme.message;
		this.isCity = theme.isCity;
		this.isCityPinyin = theme.isCityPinyin;
		this.lockType = theme.lockType;
		if (lockType > 0) {
			isLock = true;
		}
		this.isWeather = theme.isWeather;
		this.themeFolder = theme.themeFolder;
		this.musicPath = theme.musicPath;
		this.musicTitle = theme.musicTitle;
		this.themeIcon = theme.themeIcon;
		this.isEmpty = theme.isEmpty;
	}

	public POThemeSingle(JSONObject jst, String bannerTheme) {
		sthid = jst.optString("content");
		banner = jst.optString("pic");
	}


	public POThemeSingle(JSONObject obj) {
		themeName = obj.optString("themeName").replace(" ", "_");
		themeDisplayName = StringUtils.trim(obj.optString("themeDisplayName"));
		isEmpty = obj.optBoolean("isEmpty", false);

		fileExt = obj.optString("ext");

		isMv = obj.optBoolean("isMV", false);
		if (isMv) {
			musicName = obj.optString("musicName");
			musicTitle = obj.optString("musicTitle");
			if (StringUtils.isEmpty(musicTitle)) {
				musicTitle = StringUtils.trim(musicName).replace("_", " ");
			}
			category = obj.optString("musicCategory");
		}
		isMP4 = obj.optBoolean("isMP4");
		if (isMP4) {
			if (StringUtils.isEmpty(fileExt)) {
				fileExt = ".mp4";
			}
			watermarkBlendmode = obj.optString("blendmode", "BlendScreen");
		}

		isSpeed = obj.optBoolean("isSpeed", false);
		if (isSpeed) {
			speed = (float) obj.optDouble("speed", 1F);
		}

		soundType = obj.optInt("soundType");
		if (isSoundEffect()) {
			soundPitch = obj.optInt("pitch");
			soundEngine = obj.optInt("engine");
			soundVoicer = obj.optInt("voicer");
		}

		isFilter = obj.optBoolean("isFilter", false);
		if (isFilter && StringUtils.isEmpty(fileExt)) {
			fileExt = ".bmp";
		}

		JSONObject text = obj.optJSONObject("text");
		if (text != null) {
			textSize = text.optInt("textSize");
			textBold = text.optBoolean("textBold");
			textGravity = text.optString("gravity");
			textX = text.optInt("x");
			textY = text.optInt("y");
			textBackground = text.optString("background");
			textColor = text.optString("textColor");
		}
		message = obj.optString("message");
		isCity = obj.optBoolean("isCity");
		isCityPinyin = obj.optBoolean("isCityPinyin");

		lockType = obj.optInt("lockType");
		if (lockType > 0) {
			isLock = true;
		}

		//
		isWeather = obj.optBoolean("isWeather");
	}

	public void reset() {
		status = -1;
		percent = 0;
		themeUrl = "";
		isBuy = true;
	}

	public boolean isNestMusic() {
		return StringUtils.isEmpty(themeDownloadUrl);
	}

	public boolean isSoundEffect() {
		return soundType > 0;
	}

	public boolean isMV() {
		return isMv;
	}


	public boolean isFilter() {
		return isFilter;
	}

	public boolean isSpeed() {
		return isSpeed;
	}

	public boolean isWatermark() {
		return isMP4;
	}

	public boolean isEmpty() {
		return isEmpty;
	}


	public boolean isOriginalTheme() {
		return THEME_EMPTY.equals(themeName);
	}


	public boolean isStoneTheme() {
		return THEME_STONE.equals(themeName);
	}

	public String getWatermarkPath() {
		return FileUtils.concatPath(themeFolder, themeName + fileExt);
	}


	public String getFilterPath() {
		return FileUtils.concatPath(themeFolder, themeName + fileExt);
	}


	public boolean isText() {
		return textSize > 0;
	}


	public boolean isFree() {
		if (price > 0) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return String.format("themeName:%s isMv:%s lockType:%d isCity:%s", themeName, isMV() ? "true" : "false", lockType, isCity ? "true" : "false");
	}
}
