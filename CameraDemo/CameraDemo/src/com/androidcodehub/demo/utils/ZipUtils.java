package com.androidcodehub.demo.utils;


public class ZipUtils {


	public static java.util.List<java.io.File> GetFileList(String zipFileString, boolean bContainFolder, boolean bContainFile) throws Exception {
		java.util.List<java.io.File> fileList = new java.util.ArrayList<java.io.File>();
		java.util.zip.ZipInputStream inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFileString));
		java.util.zip.ZipEntry zipEntry;
		String szName = "";

		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();

			if (zipEntry.isDirectory()) {

				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				java.io.File folder = new java.io.File(szName);
				if (bContainFolder) {
					fileList.add(folder);
				}

			} else {
				java.io.File file = new java.io.File(szName);
				if (bContainFile) {
					fileList.add(file);
				}
			}
		}//end of while

		inZip.close();

		return fileList;
	}


	public static java.io.InputStream UpZip(String zipFileString, String fileString) throws Exception {
		//		android.util.Log.v("XZip", "UpZip(String, String)");
		java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipFileString);
		java.util.zip.ZipEntry zipEntry = zipFile.getEntry(fileString);

		return zipFile.getInputStream(zipEntry);

	}


	public static void UnZipFolder(String zipFileString, String outPathString) throws Exception {
		//		android.util.Log.v("XZip", "UnZipFolder(String, String)");
		java.util.zip.ZipInputStream inZip = new java.util.zip.ZipInputStream(new java.io.FileInputStream(zipFileString));
		java.util.zip.ZipEntry zipEntry;
		String szName = "";

		while ((zipEntry = inZip.getNextEntry()) != null) {
			szName = zipEntry.getName();

			if (zipEntry.isDirectory()) {

				// get the folder name of the widget
				szName = szName.substring(0, szName.length() - 1);
				java.io.File folder = new java.io.File(outPathString + java.io.File.separator + szName);
				folder.mkdirs();

			} else {

				java.io.File file = new java.io.File(outPathString + java.io.File.separator + szName);
				//				Logger.e("[ZipUtils]" + outPathString + java.io.File.separator + szName);
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				file.createNewFile();
				// get the output stream of the file
				java.io.FileOutputStream out = new java.io.FileOutputStream(file);
				int len;
				byte[] buffer = new byte[1024];
				// read (len) bytes into buffer
				while ((len = inZip.read(buffer)) != -1) {
					// write (len) byte from buffer at the position 0
					out.write(buffer, 0, len);
					out.flush();
				}
				out.close();
			}
		}//end of while

		inZip.close();

	}//end of func


	public static void ZipFolder(String srcFileString, String zipFileString) throws Exception {
		android.util.Log.v("XZip", "ZipFolder(String, String)");


		java.util.zip.ZipOutputStream outZip = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(zipFileString));


		java.io.File file = new java.io.File(srcFileString);


		ZipFiles(file.getParent() + java.io.File.separator, file.getName(), outZip);


		outZip.finish();
		outZip.close();

	}//end of func


	private static void ZipFiles(String folderString, String fileString, java.util.zip.ZipOutputStream zipOutputSteam) throws Exception {
		//		android.util.Log.v("XZip", "ZipFiles(String, String, ZipOutputStream)");

		if (zipOutputSteam == null)
			return;

		java.io.File file = new java.io.File(folderString + fileString);

			if (file.isFile()) {

			java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(fileString);
			java.io.FileInputStream inputStream = new java.io.FileInputStream(file);
			zipOutputSteam.putNextEntry(zipEntry);

			int len;
			byte[] buffer = new byte[4096];

			while ((len = inputStream.read(buffer)) != -1) {
				zipOutputSteam.write(buffer, 0, len);
			}

			zipOutputSteam.closeEntry();
		} else {

			String fileList[] = file.list();

			if (fileList.length <= 0) {
				java.util.zip.ZipEntry zipEntry = new java.util.zip.ZipEntry(fileString + java.io.File.separator);
				zipOutputSteam.putNextEntry(zipEntry);
				zipOutputSteam.closeEntry();
			}


			for (int i = 0; i < fileList.length; i++) {
				ZipFiles(folderString, fileString + java.io.File.separator + fileList[i], zipOutputSteam);
			}//end of for

		}

	}
}
