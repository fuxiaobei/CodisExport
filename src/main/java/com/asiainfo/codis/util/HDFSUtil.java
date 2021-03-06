package com.asiainfo.codis.util;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.IOUtils;

public class HDFSUtil {


    private HDFSUtil() {

    }

    /**
     * 判断路径是否存在
     *
     * @param conf
     * @param path
     * @return
     * @throws IOException
     */
    public static boolean exits(Configuration conf, String path) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        return fs.exists(new Path(path));
    }

    /**
     * 创建文件
     *
     * @param conf
     * @param filePath
     * @param contents
     * @throws IOException
     */
    public static void createFile(Configuration conf, String filePath, byte[] contents) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);
        FSDataOutputStream outputStream = fs.create(path);
        outputStream.write(contents);
        outputStream.close();
        fs.close();
    }

    /**
     * 创建文件
     *
     * @param conf
     * @param filePath
     * @param fileContent
     * @throws IOException
     */
    public static void createFile(Configuration conf, String filePath, String fileContent) throws IOException {
        createFile(conf, filePath, fileContent.getBytes());
    }

    /**
     * @param conf
     * @param localFilePath
     * @param remoteFilePath
     * @throws IOException
     */
    public static void copyFromLocalFile(Configuration conf, String localFilePath, String remoteFilePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path localPath = new Path(localFilePath);
        Path remotePath = new Path(remoteFilePath);
        fs.copyFromLocalFile(true, true, localPath, remotePath);
        fs.close();
    }

    /**
     * 删除目录或文件
     *
     * @param conf
     * @param remoteFilePath
     * @param recursive
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(Configuration conf, String remoteFilePath, boolean recursive) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        boolean result = fs.delete(new Path(remoteFilePath), recursive);
        fs.close();
        return result;
    }

    /**
     * 删除目录或文件(如果有子目录,则级联删除)
     *
     * @param conf
     * @param remoteFilePath
     * @return
     * @throws IOException
     */
    public static boolean deleteFile(Configuration conf, String remoteFilePath) throws IOException {
        return deleteFile(conf, remoteFilePath, true);
    }

    /**
     * 文件重命名
     *
     * @param conf
     * @param oldFileName
     * @param newFileName
     * @return
     * @throws IOException
     */
    public static boolean renameFile(Configuration conf, String oldFileName, String newFileName) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        Path oldPath = new Path(oldFileName);
        Path newPath = new Path(newFileName);
        boolean result = fs.rename(oldPath, newPath);
        fs.close();
        return result;
    }

    /**
     * 创建目录
     *
     * @param conf
     * @param dirName
     * @return
     * @throws IOException
     */
    public static boolean createDirectory(Configuration conf, String dirName) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        System.out.println(fs.getDefaultUri(conf));
        Path dir = new Path(dirName);
        boolean result = fs.mkdirs(dir);
        fs.close();
        return result;
    }

    /**
     * 列出指定路径下的所有文件(不包含目录)
     *
     * @param conf
     * @param basePath
     * @param recursive
     */
    public static RemoteIterator<LocatedFileStatus> listFiles(FileSystem fs, String basePath, boolean recursive) throws IOException {

        RemoteIterator<LocatedFileStatus> fileStatusRemoteIterator = fs.listFiles(new Path(basePath), recursive);

        return fileStatusRemoteIterator;
    }

    /**
     * 列出指定路径下的文件（非递归）
     *
     * @param conf
     * @param basePath
     * @return
     * @throws IOException
     */
    public static RemoteIterator<LocatedFileStatus> listFiles(Configuration conf, String basePath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        RemoteIterator<LocatedFileStatus> remoteIterator = fs.listFiles(new Path(basePath), false);
        fs.close();
        return remoteIterator;
    }

    /**
     * 列出指定目录下的文件\子目录信息（非递归）
     *
     * @param conf
     * @param dirPath
     * @return
     * @throws IOException
     */
    public static FileStatus[] listStatus(Configuration conf, String dirPath) throws IOException {
        FileSystem fs = FileSystem.get(conf);
        FileStatus[] fileStatuses = fs.listStatus(new Path(dirPath));
        fs.close();
        return fileStatuses;
    }


    /**
     * 读取文件内容
     *
     * @param conf
     * @param filePath
     * @return
     * @throws IOException
     */
    public static String readFile(Configuration conf, String filePath) throws IOException {
        String fileContent = null;
        FileSystem fs = FileSystem.get(conf);
        Path path = new Path(filePath);
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = fs.open(path);
            outputStream = new ByteArrayOutputStream(inputStream.available());
            IOUtils.copyBytes(inputStream, outputStream, conf);
            fileContent = outputStream.toString();
        } finally {
            IOUtils.closeStream(inputStream);
            IOUtils.closeStream(outputStream);
            fs.close();
        }
        return fileContent;
    }
    
    /**
     * 将内容写入到文件里
     * @param conf
     * @param filePath
     * @throws IOException
     */
    public static void writeFile(Configuration conf, String filePath,String contents)throws IOException{
    	FileSystem fs = FileSystem.get(conf);
    	FSDataOutputStream fsout = fs.create(new Path(filePath));
    	BufferedWriter out = null;
    	try{
    		out = new BufferedWriter(new OutputStreamWriter(fsout,"utf-8"));
    		out.write(contents);
    		out.newLine();
    		out.flush();
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally{
    		IOUtils.closeStream(out);
    		IOUtils.closeStream(fsout);
    		fs.close();
    	}
    }
    
    public static void main(String[] args){
    	Configuration conf = new Configuration();
    	
    	conf.set("fs.defaultFS", "hdfs://ochadoopcluster2");
		   conf.set("dfs.nameservices", "ochadoopcluster2");
		   conf.set("dfs.ha.namenodes.ochadoopcluster2", "nn1,nn2");
		   conf.set("dfs.namenode.rpc-address.ochadoopcluster2.nn1", "YSZQDJ1:8020");
		   conf.set("dfs.namenode.rpc-address.ochadoopcluster2.nn2", "YSZQDJ2:8020");
		   conf.set("dfs.client.failover.proxy.provider.ochadoopcluster2", 
				   "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
		   conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem");
		
		 try {
//			HDFSUtil.createDirectory(conf, "/fust1111");
			System.out.println(HDFSUtil.readFile(conf, "/position/sitegbase/20160309/YSZQDJ1063801457493018644"));
//			FileStatus[] file = HDFSUtil.listStatus(conf, "/position/sitegbase/20160309/YSZQDJ1063801457493018644");
//			for(int i=0;i<file.length;i++){
//				FileStatus f = file[i];
//				System.out.println(f.getPath().getName());
//			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    

}