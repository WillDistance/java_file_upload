package com.thinkive.demo.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.thinkive.base.config.Configuration;
import com.thinkive.base.jdbc.DataRow;
import com.thinkive.base.util.DateHelper;
import com.thinkive.base.util.StringHelper;
import com.thinkive.gateway.v2.result.Result;

/**
 * 
 * @����: �ļ��ϴ�servlet�������������ļ���С�ϴ���֤������ļ��Ƿ��ϴ�����
 * @��Ȩ: Copyright (c) 2019 
 * @��˾: ˼�ϿƼ� 
 * @����: ����
 * @�汾: 1.0 
 * @��������: 2019��11��13�� 
 * @����ʱ��: ����1:36:01
 */
@SuppressWarnings("serial")
public class UploadHandleServlet extends HttpServlet
{
    
    private static Logger      logger                = Logger.getLogger(UploadHandleServlet.class);
    
    private static long        fileSizeMax           = Long.parseLong(Configuration.getString("h5_upload.fileSizeMax", "2097152")); //�����ļ������ֵ��Ĭ��5M
                                                                                                                   
    private static long        sizeMax               = Long.parseLong(Configuration.getString("h5_upload.sizeMax", "10485760"));    //�����ϴ��Ķ���ļ������ֵ,Ĭ��10M
                                                                                                                   
    private static String      savePath              = Configuration.getString("h5_upload.savePath");             //�ļ��洢·��
                                                                                                                   
    private static String      tempPath              = Configuration.getString("h5_upload.tempPath");             //�ϴ���ʱ�ļ����·��
                                                                                                                   
    private static String      dbSavePathStartFolder = Configuration.getString("h5_upload.dbSavePathStartFolder"); //���ݿ�洢·����ʼ�ļ���
                                                                                                                   
    private static Set<String> fileTypeSet           = new HashSet<String>();                                     //�����ϴ����ļ����ͣ��á�|���ָ���ȫСд
                                                                                                                   
    private static boolean     isDeleteTempFile      = true;                                                      //�Ƿ�ɾ����ʱ�ļ�
                                                                                                                   
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        //�����ļ��������ʱ�ļ���Ĭ��·����WebRoot�µ�upload�ļ��� �� temp�ļ���
        if ( StringHelper.isBlank(savePath) )
        {
            savePath = this.getServletContext().getRealPath("/upload");
        }
        if ( StringHelper.isBlank(tempPath) )
        {
            tempPath = this.getServletContext().getRealPath("/temp");
        }
        //������ʱĿ¼
        File tmpFile = new File(tempPath);
        if ( !tmpFile.exists() )
        {
            tmpFile.mkdir();
        }
        if ( StringHelper.isBlank(dbSavePathStartFolder) )
        {
            dbSavePathStartFolder = "upload";
            savePath = this.getServletContext().getRealPath("/upload");
        }
        if ( StringHelper.isNotBlank(Configuration.getString("h5_upload.isDeleteTempFile")) )
        {
            isDeleteTempFile = Configuration.getBoolean("h5_upload.isDeleteTempFile");
        }
        //�����ϴ��ļ�����
        String fileType = Configuration.getString("h5_upload.fileType");
        fileTypeSet.addAll(Arrays.asList(fileType.split("\\|")));
        
        Result result = new Result();
        //����
        DataRow resultData = new DataRow();
        
        try
        {
            /**
             * ʹ��Apache��commons-fileupload�ļ��ϴ���������ļ��ϴ����裺
             */
            //1������һ�������ļ������������û���ÿ���ϴ����ļ�����һ��FileItem����
            DiskFileItemFactory factory = new DiskFileItemFactory();
            
            factory.setRepository(tmpFile);//�����ϴ�ʱ���ɵ���ʱ�ļ��ı���Ŀ¼
            factory.setSizeThreshold(1024 * 100);//���û������Ĵ�СΪ100KB�����ϴ����ļ���С�����������Ĵ�Сʱ���ͻ�����һ����ʱ�ļ���ŵ�ָ������ʱĿ¼���С������ָ������ô�������Ĵ�СĬ����10KB
            
            //2����ָ���Ĺ�������һ���ļ��ϴ�������
            ServletFileUpload upload = new ServletFileUpload(factory);
            
            //�����ļ��ϴ����ȣ��������FileUploadBase�������ж���ķ���
            upload.setProgressListener(new ProgressListener()
            {
                /**
                 * 
                 * @����������������״̬��Ϣ��
                 * @���ߣ�����
                 * @ʱ�䣺2019��11��13�� ����10:12:59
                 * @param pBytesRead  ��ĿǰΪֹ�Ѷ�ȡ�����ֽ���
                 * @param pContentLength ���ڶ�ȡ���ֽ����������������δ֪�������Ϊ-1
                 * @param pItems  ��ǰ���ڶ�ȡ���ֶα�š���0 =��ĿǰΪֹû���κ���Ŀ��1 =���ڶ�ȡ��һ����Ŀ��...��
                 */
                public void update(long pBytesRead, long pContentLength, int pItems)
                {
                    //System.out.println("�ļ���СΪ��" + pContentLength + ",��ǰ�Ѵ���" + pBytesRead);
                }
            });
            
            upload.setFileSizeMax(fileSizeMax);//�����ϴ������ļ��Ĵ�С������ֽ���
            
            upload.setSizeMax(sizeMax);//�����ϴ��ļ�����������ֽ���
            
            upload.setHeaderEncoding("UTF-8");//����ϴ��ļ�������������
            
            //3���ж��ύ�����������Ƿ����ϴ���������
            if ( !ServletFileUpload.isMultipartContent(request) )
            {
                //��������ݲ���һ�������ݣ���ǰ����Ҫʹ��FormData����ģ����ύ
                result.setErr_no( -1);
                result.setErr_info("��������ݲ���һ��������");
                responseJSONData(response, result);
                return;
            }
            
            //4��ʹ��ServletFileUpload�����������ϴ����ݣ�����������ص���һ��List<FileItem>���ϣ�ÿһ��FileItem��Ӧһ��Form����������
            List<FileItem> list = upload.parseRequest(request);
            for (FileItem item : list)
            {
                //���fileitem�з�װ������ͨ�����������
                if ( item.isFormField() )
                {
                    String name = item.getFieldName();
                    String value = item.getString("UTF-8");
                    //value = new String(value.getBytes("iso8859-1"),"UTF-8");
                    logger.info(name + "=" + value);
                }
                else
                //���fileitem�з�װ�����ϴ��ļ�
                {
                    
                    String filename = item.getName();
                    logger.error("�ļ�����" + filename);
                    if ( filename == null || filename.trim().equals("") )
                    {
                        continue;
                    }
                    //ע�⣺��ͬ��������ύ���ļ����ǲ�һ���ģ���Щ������ύ�������ļ����Ǵ���·���ģ��磺  c:\a\b\1.txt������Щֻ�ǵ������ļ������磺1.txt
                    //�����ȡ�����ϴ��ļ����ļ�����·�����֣�ֻ�����ļ�������
                    filename = filename.substring(filename.lastIndexOf("\\") + 1);
                    
                    //�õ��ϴ��ļ�����չ���������ϴ����ļ�����
                    String fileExtName = "";
                    if ( filename.lastIndexOf(".") > -1 )
                    {
                        fileExtName = filename.substring(filename.lastIndexOf(".") + 1);
                    }
                    
                    logger.info("�ϴ����ļ�����չ���ǣ�" + fileExtName);
                    //��չ��Ϊ�գ�������չ�����������ļ�����ֹ�ϴ�
                    if ( StringHelper.isBlank(fileExtName) || !fileTypeSet.contains(fileExtName.toLowerCase()) )
                    {
                        logger.error("��ֹ�ϴ�fileExtName" + fileExtName + "���͵��ļ�");
                        //resultData.set(filename, "��ֹ�ϴ�fileExtName"+fileExtName+"���͵��ļ�");
                        continue;
                    }
                    
                    //��ȡitem�е��ϴ��ļ���������
                    InputStream in = item.getInputStream();
                    
                    String realSavePath = makeFileSavePath(fileExtName, savePath);//�õ��ļ��Ĵ洢Ŀ¼
                    
                    FileOutputStream out = new FileOutputStream(realSavePath);
                    
                    byte buffer[] = new byte[1024];
                    int len = 0;
                    //ѭ�������������뵽�����ֽ�����
                    while ((len = in.read(buffer)) > 0)
                    {
                        out.write(buffer, 0, len);
                    }
                    //�ر�������
                    in.close();
                    //�ر������
                    out.close();
                    //ɾ����ʱ�ļ�
                    if ( isDeleteTempFile )
                    {
                        item.delete();
                    }
                    resultData.set(filename, realSavePath.substring(realSavePath.indexOf(File.separator + dbSavePathStartFolder)));
                }
            }
        }
        catch (FileUploadBase.FileSizeLimitExceededException e)
        {
            result.setErr_no( -2);
            result.setErr_info("�����ļ��������ֵ������");
            responseJSONData(response, result);
            return;
        }
        catch (FileUploadBase.SizeLimitExceededException e)
        {
            result.setErr_no( -3);
            result.setErr_info("�ϴ��ļ����ܵĴ�С�������Ƶ����ֵ������");
            responseJSONData(response, result);
            return;
        }
        catch (Exception e)
        {
            logger.error("��upload���ļ��ϴ�ʧ��", e);
            result.setErr_no( -4);
            result.setErr_info("�ļ��ϴ�ʧ��");
            responseJSONData(response, result);
            return;
        }
        result.setErr_no(0);
        result.setErr_info("success");
        result.setResult(resultData);
        responseJSONData(response, result);
    }
    
    public void responseJSONData(HttpServletResponse response, Result result) throws IOException
    {
        JSONObject json = new JSONObject();
        json.put("err_info", result.getErr_info());
        json.put("err_no", result.getErr_no());
        json.put("data", JSON.toJSON(result.getData()));
        response.getWriter().print(json);
    }
    
    /**
     * 
     * @�����������ļ�����·��
     * @���ߣ�����
     * @ʱ�䣺2019��11��13�� ����10:51:18
     * @param fileExtName �ļ���չ��
     * @param savePath �ļ��洢·��
     * @return �µĴ洢Ŀ¼
     */
    private String makeFileSavePath(String fileExtName, String savePath)
    {
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + fileExtName;
        String date = DateHelper.formatDate(new Date(), "yyyyMMdd");
        //�����µı���Ŀ¼
        String dir = savePath + File.separator + date;
        
        //File�ȿ��Դ����ļ�Ҳ���Դ���Ŀ¼
        File file = new File(dir);
        //���Ŀ¼������
        if ( !file.exists() )
        {
            //����Ŀ¼
            file.mkdirs();
        }
        return dir + File.separator + fileName;
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
}
