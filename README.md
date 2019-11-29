# java_file_upload
java文件上传，从前端到后端，完整demo。兼容iOS

运行环境：jdk1.6 eclipse jetty

如果需要修改上传文件的路径，可以修改resources下的configuration.xml文件的配置项
<category name="h5_upload" description="文件上传配置">
	<item name="savePath" value="D:\develop\workspace\file-upload-download\src\main\webapp\upload" description="文件存储路径" />
	<item name="tempPath" value="D:\develop\workspace\file-upload-download\src\main\webapp\temp" description="上传临时文件存放路径" />
	<item name="fileType" value="jpg|png|zip|mp4" description="允许上传的文件类型，用“|”分隔，全小写" />
	<item name="sizeMax" value="10485760" description="单次上传的多个文件的最大值,默认10M" />
	<item name="fileSizeMax" value="2097152" description="单个文件的最大值，默认2M" />
	<item name="dbSavePathStartFolder" value="upload" description="数据库存储路径起始文件夹" />
	<item name="isDeleteTempFile" value="true" description="是否删除临时文件" />
</category>


本地启动，访问：http://127.0.0.1:8080/uploadTest.html

