# se-invigilation


### Dingtalk
比较了一下钉钉的通知方式，最终选择结合工作通知+日程实现。

**DING**  
DING一下，免费标准版不支持，需专业版以上。  

**待办任务**  
待办任务没找到设置开始时间与提醒时间。

**工作通知**  
禁止发布内容相同的通知，添加时间戳解决。  
钉钉超链接通知使用内置浏览器打开，不支持跳转至外部浏览器，内置浏览器内核版本过低不支持项目前端项目。
对低版本内核编译会影响正常浏览器的使用，只能放弃多媒体通知类型。

**日程**   
日程不支持DING，不显示通知。  
日程可设置起止时间，置于钉钉日历，支持多次提醒，暂定24+2小时提醒；  

### Others
数据前后端处理的思考   
前端，可减轻后端服务器压力；数据组装过于分散；前端组装数据较不安全；增加网络数据传输量；  
后端，增加了后端服务器压力；相关数据在后端数据库提取，较安全；
在SQL/NoSQL混合式开发模式下，可能有大量数据需要序列/反序列化，没有前端方便；异步反应式编程不利于维护；  