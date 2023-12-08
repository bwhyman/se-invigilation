# se-invigilation
### Introduction
监考分配管理及钉钉通知系统

- 关联钉钉账号
- 导入全院教师/独立教师课表
- 监考批量下发
- 分析考试时间与教师授课时间冲突，根据教师监考数量/当日授课/当日监考等综合情况给出分配建议
- 导入导出监考信息
- 向专业负责人发送钉钉监考分配提醒通知
- 向监考教师发送：1次即时工作通知，1次即时日程通知，2次(24+2小时)日程提醒
- 监考重要信息随状态变化锁定
- 监考取消/重置等的钉钉通知及日程撤回

### Update
#### 2023.12.08
基于日期/开始时间/授课教师排序监考信息，确保同一授课教师的相同考试在列表中顺序排序。  

#### 2023.11.14
重构学院直接分配监考，添加复合索引提高查询效率。
将钉钉日程提醒时间修改为监考前一天早9点+2小时。避免原24小时提醒时第二天晚上的监考，教师关闭钉钉无法提前一天收到钉钉提醒。前端计算实现。    
由第一位监考教师创建钉钉日程，避免原由专业负责人创建日程时收到不必要的监考日程提醒。但第一监考教师没有`钉钉日程确认`提醒。    
剪裁。从当前监考剪裁出一个新监考，监考教师跨专业授课时，便于学院直接分配。前端实现。  

#### 2023.11.04
处理以日期/开始时间正序排序，其他检索以日期倒序开始时间正序排序。  

#### 2023.11.02
修复若干bug。   
重组部分接口，增强了接口扩展性/安全性/弹性。


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
向自己发送的日程没有即时提醒通知，向其他人发送有。~~日程不支持DING，不显示通知。~~  
日程可设置起止时间，置于钉钉日历，支持多次提醒，暂定24+2小时提醒；  

### Others
数据前后端处理的思考   
前端，可减轻后端服务器压力；数据组装过于分散松散；前端组装数据较不安全；增加网络数据传输量；  
后端，增加了后端服务器压力；相关数据在后端数据库提取，较安全；
在SQL/NoSQL混合式开发模式下，可能有大量数据需要序列/反序列化，没有前端方便；异步反应式编程不利于维护；  