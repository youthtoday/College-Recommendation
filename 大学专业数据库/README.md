大学专业数据库
=================
## 大学专业信息
### 学校表（college）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 school_id  | mediumint(9) | 否  | 主键 | 教育部唯一标识的学校代码 
 sname  | varchar(30) | 否  | 唯一约束 | 学校名称  
 snameEn  | varchar(100) | 否  |  | 英文名 
 province  | varchar(3) | 否  |  | 所处省份
 belong  | varchar(20) | 否  |  | 隶属部门 
 type  | varchar(3) | 否  |  | 院校类型  
 adminType  | varchar(10) | 否  |  | 办学类型 
 is985  | tinyint (1) | 否  |  | 985，1为是，0为否  
 is211  | tinyint (1) | 否  |  | 211，1为是，0为否      
 dual_class  | tinyint (1) | 否  |  | 双一大学，同上   
 phone  | varchar(100) | 是  |  | 官方电话   
 email  | varchar(50) | 是  |  | 电子邮箱   
 school_site  | varchar(50) | 否  |  | 官方网址   
 RKrank  | smallint(6) | 是  |  | 软科排名   
 
### 专业表（major）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 mcode| varchar(10) | 否  | 主键 | 专业代码 
 mname  | varchar(20) | 否  | 唯一约束 | 专业名称  
 mclassid| smallint(6) | 否  | 外键 | 所属学科大类代码 
 edu_type| varchar(2) | 否  |  | 学历类型
 limit_year| varchar(5) | 否  |  | 修业年限
 degree| varchar(20) | 否  |  | 授予学位  
 sel_adv| varchar(60) | 否  |  | 选考学科建议 
 job| text | 否  |  |相关就业  
 direction| varchar(60) | 否  |  | 考研方向      
 celebrity  | varchar(50)  | 否  |  | 相关社会名人   
 is_what  | varchar(255) | 否  |  | 简介  
  
 ### 学科大类表（class）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 classid| smallint(6) | 否  | 主键 | 学科大类代码 
 classname  | varchar(10) | 否  | 唯一约束 | 学科大类名称  
 clcid| varchar(2) | 否  | 外键 | 该大类所属学科门类代码 
   
 ### 学科门类表（category）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 cid| varchar(2) | 否  | 主键 | 学科门类代码 
 cname  | varchar(10) | 否  | 唯一约束 | 学科门类名称  
   
 ### 开设信息表（college_major）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 school_id  | mediumint(9) | 否  | 主键，外键 | 学校代码 
 mcode| varchar(10) | 否  | 主键，外键 | 专业代码 
 mname  | varchar(20) | 否  |  | 学校开设的该专业名称 
 nationfeature| tinyint (1) | 否  |  | 是否为国家特色专业 
    
 ### 学科评分表（college_class）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 school_id  | mediumint(9) | 否  | 主键，外键 | 学校代码 
 classid| smallint(6) | 否  | 主键，外键 | 学科大类代码 
 rating| decimal(12,2) | 否  |  | 学科评估的分数 
 ## 推荐结果
 ### 学校推荐结果表（college_rec）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 school_id  | mediumint(9) | 否  | 主键，外键 | 主体学校代码 
 sim1| mediumint(9) | 否  |  | 推荐学校1学校代码 
 sim2| mediumint(9) | 否  |  | 推荐学校2学校代码 
 sim3| mediumint(9) | 否  |  | 推荐学校3学校代码 
 sim4| mediumint(9) | 否  |  | 推荐学校4学校代码 
 sim5| mediumint(9) | 否  |  | 推荐学校5学校代码 
 sim6| mediumint(9) | 否  |  | 推荐学校6学校代码 
 sim7| mediumint(9) | 否  |  | 推荐学校7学校代码 
 sim8| mediumint(9) | 否  |  | 推荐学校8学校代码 
 sim9| mediumint(9) | 否  |  | 推荐学校9学校代码 
    
 ### 学校专业推荐结果表（college_major_rec）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 school_id  | mediumint(9) | 否  | 主键，外键 | 主体学校代码 
 mcode|mediumint(9) | 否  | 主键，外键 | 主体学校专业代码 
 recommend| varchar(255) | 否  |  | 推荐结果

## 用户模块
### 用户信息表（user）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 user| int(11) | 否  | 主键 | 用户编号 
 mcode|mediumint(9) | 否  | | 用户名 
 password| varchar(64) | 否  |  | 用户密码
 expect| smallint(6) | 否  |  | 高考分数
 gender| varchar(1) | 否  |  | 性别
 nationality| varchar(10) | 否  |  | 民族  
 province| varchar(10) | 否  |  | 所处省市地区
 subjects| varchar(20) | 否  |  | 所选三门学科
 
 ### 角色表（role）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 role| smallint(6) | 否  | 主键 | 角色编号 
 name|varchar(20) | 否  | |角色名 
  
 ### 权限表（permission）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 p_id| smallint(6) | 否  | 主键 | 权限编号 
 p_name|varchar(20) | 否  | |权限名 
 p_code|varchar(30) | 否  | |权限码
  
 ### 分配角色表（role_user）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 u_id| int(11) | 否  | 主键，外键 | 用户编号 
 r_id| smallint(6) | 否  | 主键，外键| 角色编号
  
 ### 分配权限表（role_permission）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 r_id| smallint(6) | 否  | 主键，外键 | 角色编号 
 p_id| smallint(6) | 否  | 主键，外键| 权限编号
  
 ### 点击记录表（syslog）
 字段名  | 数据类型  | 允许NULL值  |  约束  |  注释  |  
 ---- | ----- | ------ | ----- | ------  
 u_id| int(11) | 否  | 主键，外键 | 用户编号 
 school_id| mediumnt(9) | 否  | 主键，外键| 点击对象的学校代码
 create_time| timestamp| 否  |  | 点击的时间
 
