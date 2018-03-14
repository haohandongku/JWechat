package com.dcits.app.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.dcits.app.data.DataObject;

public class FileUtils {
	private static final Log LOG = LogFactory.getLog(FileUtils.class);
	private static String access_bucketName;
	private static String access_id;
	private static String access_key;
	private static String oss_endpoint;
	private static String oss_cname;
	private static String proxyHost;
	private static int proxyPort;
	private static String isProxy;
	private static String expireDate;
	private static String storageType;
	private static String localPath;
	private static String ftp_host;
	private static int ftp_port;
	private static String ftp_username;
	private static String ftp_password;
	private static String key = "http://www.leshui365.com";

	public static DataObject upLoadFile(FileItem fileItem, String filePath,
			String fileName) throws Throwable {
		InputStream in = fileItem.getInputStream();
		return upLoadFile(IOUtils.toByteArray(in), filePath, fileName);
	}

	public static DataObject upLoadFile(byte[] input, String filePath,
			String fileName) throws Throwable {
		if ("aliyun".equals(storageType)) {
			return upLoadFileToCloud(input, filePath, fileName);
		} else if ("ftp".equals(storageType)) {
			return upLoadFileToFtp(input, filePath, fileName);
		} else {
			return upLoadFileToLocal(input, filePath, fileName);
		}
	}

	public static InputStream downLoadFile(String key) {
		if ("aliyun".equals(storageType)) {
			return downLoadFileFromCloud(key);
		} else if ("ftp".equals(storageType)) {
			return downLoadFileFromFtp(key);
		} else {
			return downLoadFileFromLocal(key);
		}
	}

	public static void deleteFile(String key) {
		if ("aliyun".equals(storageType)) {
			deleteFileOfCloud(key);
		} else if ("ftp".equals(storageType)) {
			deleteFileOfFtp(key);
		} else {
			deleteFileOfLocal(key);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataObject upLoadFileToCloud(byte[] input, String filePath,
			String fileName) throws Throwable {
		if (filePath.indexOf("/") == 0) {
			filePath = filePath.substring(1);
		}
		if (!filePath.endsWith("/")) {
			filePath = filePath + "/";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(filePath).append(fileName);
		String key = sb.toString();
		String bucketName = generateBucketName();
		OSSClient client = createOSSClient();
		setBucketPublicReadable(client, bucketName);
		InputStream in = new ByteArrayInputStream(input);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setContentLength(in.available());
		client.putObject(bucketName, key, in, objectMetadata);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date expiration = dateFormat.parse(expireDate);
		URL url = client.generatePresignedUrl(bucketName, key, expiration);
		String urlStr = url.toString();
		if (!oss_endpoint.equals(oss_cname) && urlStr.indexOf("?Expires") != -1) {
			int beginIndex = urlStr.indexOf("?Expires");
			String suffixStr = urlStr.substring(beginIndex);
			urlStr = (new StringBuffer()).append(oss_cname).append("/")
					.append(key).append(suffixStr).toString();
		}
		if (urlStr.indexOf("-internal") > 0) {
			urlStr = urlStr.replaceFirst("-internal", "");
		}
		Map map = new HashMap();
		InputStream in2 = new ByteArrayInputStream(input);
		String md5 = MD5Utils.getStreamMD5(in2);
		map.put("MD5", md5);
		map.put("KEY", key);
		map.put("URL", urlStr);
		return new DataObject(map);
	}

	public static InputStream downLoadFileFromCloud(String key) {
		String bucketName = generateBucketName();
		OSSClient client = createOSSClient();
		InputStream in = null;
		try {
			OSSObject ossObject = client.getObject(bucketName, key);
			in = ossObject.getObjectContent();
		} catch (Throwable e) {
			in = null;
		}
		return in;
	}

	public static void deleteFileOfCloud(String key) {
		String bucketName = access_bucketName;
		OSSClient client = createOSSClient();
		client.deleteObject(bucketName, key);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataObject upLoadFileToFtp(byte[] input, String filePath,
			String fileName) throws Throwable {
		String root = localPath;
		if (!root.endsWith("/")) {
			root = root + "/";
		}
		if (filePath.indexOf("/") == 0) {
			filePath = filePath.substring(1);
		}
		if (!filePath.endsWith("/")) {
			filePath = filePath + "/";
		}
		StringBuffer sb = new StringBuffer();
		sb.append(filePath).append(fileName);
		String key = sb.toString();
		FTPClient ftpClient = createFtpClient();
		connectFtp(ftpClient);
		ftpClient.enterLocalPassiveMode();
		ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
		ftpClient.setControlEncoding("GBK");
		createFtpDirectory(ftpClient, localToFtp(root + filePath));
		InputStream in = new ByteArrayInputStream(input);
		ftpClient.storeFile(localToFtp(root + key), in);
		disConnectFtp(ftpClient);
		Map map = new HashMap();
		InputStream in2 = new ByteArrayInputStream(input);
		String md5 = MD5Utils.getStreamMD5(in2);
		map.put("MD5", md5);
		map.put("KEY", key);
		map.put("URL", "");
		return new DataObject(map);
	}

	public static InputStream downLoadFileFromFtp(String key) {
		FTPClient ftpClient = createFtpClient();
		InputStream in = null;
		String root = localPath;
		if (!root.endsWith("/")) {
			root = root + "/";
		}
		try {
			connectFtp(ftpClient);
			ftpClient.enterLocalPassiveMode();
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			FTPFile[] files = ftpClient.listFiles(localToFtp(root + key));
			if (files.length == 0) {
				return in;
			} else {
				in = ftpClient.retrieveFileStream(localToFtp(root + key));
			}
			disConnectFtp(ftpClient);
		} catch (Throwable e) {
			in = null;
		}
		return in;
	}

	public static void deleteFileOfFtp(String key) {
		FTPClient ftpClient = createFtpClient();
		String root = localPath;
		if (!root.endsWith("/")) {
			root = root + "/";
		}
		try {
			connectFtp(ftpClient);
			ftpClient.deleteFile(localToFtp(root + key));
		} catch (Throwable e) {
			LOG.error(e);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static DataObject upLoadFileToLocal(byte[] input, String filePath,
			String fileName) throws Throwable {
		InputStream in = new ByteArrayInputStream(input);
		String root = localPath;
		if (!root.endsWith("/")) {
			root = root + "/";
		}
		if (filePath.indexOf("/") == 0) {
			filePath = filePath.substring(1);
		}
		if (!filePath.endsWith("/")) {
			filePath = filePath + "/";
		}
		File fileDir = new File(root + filePath);
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		File file = new File(root + filePath, fileName);
		OutputStream out = new FileOutputStream(file);
		out.write(input);
		out.close();
		Map map = new HashMap();
		String md5 = MD5Utils.getStreamMD5(in);
		String key = filePath + fileName;
		map.put("MD5", md5);
		map.put("KEY", key);
		map.put("URL", "");
		return new DataObject(map);
	}

	public static InputStream downLoadFileFromLocal(String key) {
		String root = localPath;
		if (!root.endsWith("/")) {
			root = root + "/";
		}
		InputStream in = null;
		File file = new File(root + key);
		try {
			in = new FileInputStream(file);
		} catch (Throwable e) {
			in = null;
		}
		return in;
	}

	public static void deleteFileOfLocal(String key) {
		String root = localPath;
		if (!root.endsWith("/")) {
			root = root + "/";
		}
		File fileDir = new File(root + key);
		fileDir.delete();
	}

	private static String generateBucketName() {
		return access_bucketName;
	}

	private static OSSClient createOSSClient() {
		ClientConfiguration clientConfiguration = new ClientConfiguration();
		if ("YES".equals(isProxy)) {
			clientConfiguration.setProxyHost(proxyHost);
			clientConfiguration.setProxyPort(proxyPort);
		}
		OSSClient client = new OSSClient(oss_endpoint, access_id, access_key,
				clientConfiguration);
		return client;
	}

	private static void setBucketPublicReadable(OSSClient client,
			String bucketName) {
		boolean flag = client.doesBucketExist(bucketName);
		if (!flag) {
			client.createBucket(bucketName);
		}
		client.setBucketAcl(bucketName, CannedAccessControlList.Private);
	}

	private static boolean connectFtp(FTPClient ftpClient) throws Throwable {
		ftpClient.connect(ftp_host, ftp_port);
		ftpClient.setControlEncoding("GBK");
		if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			if (ftpClient.login(ftp_username, ftp_password)) {
				return true;
			}
		}
		disConnectFtp(ftpClient);
		return false;
	}

	private static boolean createFtpDirectory(FTPClient ftpClient,
			String filePath) throws Throwable {
		if (!filePath.equalsIgnoreCase("/")
				&& !ftpClient.changeWorkingDirectory(filePath)) {
			int start = 0;
			int end = 0;
			if (filePath.startsWith("/")) {
				start = 1;
			} else {
				start = 0;
			}
			end = filePath.indexOf("/", start);
			while (true) {
				String subDirectory = filePath.substring(start, end);
				if (!ftpClient.changeWorkingDirectory(subDirectory)) {
					if (ftpClient.makeDirectory(subDirectory)) {
						ftpClient.changeWorkingDirectory(subDirectory);
					} else {
						return false;
					}
				}
				start = end + 1;
				end = filePath.indexOf("/", start);
				if (end <= start) {
					break;
				}
			}
		}
		return true;
	}

	private static void disConnectFtp(FTPClient ftpClient) throws Throwable {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
		}
	}

	private static FTPClient createFtpClient() {
		FTPClient ftpClient = new FTPClient();
		return ftpClient;
	}

	private static String localToFtp(String path) throws Throwable {
		return new String(path.getBytes("GBK"), "iso-8859-1");
	}

	public void setAccess_bucketName(String access_bucketName) {
		FileUtils.access_bucketName = access_bucketName;
	}

	public void setAccess_id(String access_id) {
		FileUtils.access_id = access_id;
	}

	public void setAccess_key(String access_key) {
		byte[] buff = Base64Utils.deEncode(access_key);
		buff = ThreeDesUtils.decryptMode(key.getBytes(), buff);
		access_key = new String(buff);
		FileUtils.access_key = access_key;
	}

	public void setOss_endpoint(String oss_endpoint) {
		FileUtils.oss_endpoint = oss_endpoint;
	}

	public void setOss_cname(String oss_cname) {
		FileUtils.oss_cname = oss_cname;
	}

	public void setProxyHost(String proxyHost) {
		FileUtils.proxyHost = proxyHost;
	}

	public void setProxyPort(int proxyPort) {
		FileUtils.proxyPort = proxyPort;
	}

	public void setIsProxy(String isProxy) {
		FileUtils.isProxy = isProxy;
	}

	public void setExpireDate(String expireDate) {
		FileUtils.expireDate = expireDate;
	}

	public void setStorageType(String storageType) {
		FileUtils.storageType = storageType;
	}

	public void setLocalPath(String localPath) {
		FileUtils.localPath = localPath;
	}

	public void setFtp_host(String ftp_host) {
		FileUtils.ftp_host = ftp_host;
	}

	public void setFtp_port(int ftp_port) {
		FileUtils.ftp_port = ftp_port;
	}

	public void setFtp_username(String ftp_username) {
		FileUtils.ftp_username = ftp_username;
	}

	public void setFtp_password(String ftp_password) {
		FileUtils.ftp_password = ftp_password;
	}

}