## 功能描述

​本系统使用scala语言，主要是完成了基于Spark对于全国大学学科开设数据的推荐计算。其中Spark采用 Spark on Yarn模式，使用Spark-submit提交代码到集群执行。

程序的执行入口为src/scala/com/nihe/spark/app/update/UpdateRecommendation.scala

Spark主要用作数据清洗工作，使用方法为设置一个sparkJobDB的数据库作为输入，在里面存储如下数据表，详见数据库说明：
1. all_college_major 所有的存储大学开设专业信息
2. class_major 学科专业对照情况
3. college_major 大学专业开设情况
4. discipline_ranking 学科评估分数
5. major 所有开设的学科信息

经过计算后输出一个推荐信息数据库recDB，输出后的数据库中大学用教育部唯一标示的五位ID代码即school_id指代，学科用教育部唯一标识的ID即class_id指代，专业用教育部唯一标识ID即mcode指代：
1. college_major_rating 数据清洗后的大学学科排名数值
2. name_ier 大学和其相似大学
3. corr_ier 大学和其相似大学相似度
4. mcodeSet 学科下属专业集
5. FPGrowth 学科下专业频繁项挖掘计算关联度倒序排序
6. SMRecommendation 大学学科相似推荐，推荐内容"："前是大学ID，后面推荐的是用"，"分隔的专业。

## 环境依赖

#### 1.系统环境
Linux服务器：三台8核8GUbuntu 18.04服务器 


#### 2.各个组件版本

| 组件          | x版x本x |
| ------------- | ------- |
| spark         | 3.2.0   |
| Java          |1.8.0_202|
| Hadoop        | 3.3.1   |
| scala         | 2.12    |
| mysql         | 5.7.3   |


#### 3.项目部署

1. 安装Hadoop、Spark，其中Yarn注意更改DefaultResourceCalculator为DominantResourceCalculator，否则不能给Spark的Executor分配多个vCore。
2. 开启Hadoop和Yarn，dfs-start.sh，yarn-start.sh，
3. 开启Spark，start-master.sh，start-workers.sh
4. 将项目打包成jar并上传到HDFS文件系统
5. 控制台执行如下命令启动Spark任务程序
```shell
spark-submit \
--class [执行主类路径,也就是程序入口] \
--master yarn[Yarn资源调度] \
--deploy-mode cluster[集群模式] \
--executor-cores=[自定义每个Executor核数] \ 
--num-executors=[自定义Executor个数]  \
--executor-memory=[自定义Executor内存大小] \
--conf spark.default.parallelism=[自定义任务并行度] \
[添加主类在hdfs上的位置,这里是文件路径] \
"sparkJobDB"[输入数据库] \
"recDB"[输出数据库]
```

## 项目结构

#### 1.CollegeRecommendation_Sparkjob的目录结构
```
├─src                                          //源文件
│   ├─main                                     //主程序    
│   │  │  README.md                            //文本
│   │  ├─resources                             //配置资源文件
│   │  │  └─dataset                            //本地暂存数据
│   │  └─scala                                 //scala源码
│   │      └─com                                
│   │         └─nihe
│   │            ├─spark
│   │            │  ├─app                      //执行模块
│   │            │  │  ├─doprocess             //处理方法
│   │            │  │  ├─function              //处理函数
│   │            │  │  └─update                //程序入口和rest api后端接口
│   │            │  ├─common                   //创建结构
│   │            │  ├─dbprocess                //数据清洗
│   │            │  ├─service                  //基础服务
│   │            │  │  └─impl                  //服务实现
│   │            │  └─util                     //基础工具
│   │            └─test                        //测试                        
│   └─test                                     //测试
│       └─java
└─pom.xml	                               //依赖库
```
#### 2.具体说明
1. app
```
app
│  
├─doprocess
│      GetEvaluation.scala              //对算法进行评估
│      GetFPGrowth.scala                //专业频繁项挖掘
│      GetRec.scala                     //计算大学专业推荐
│      GetSim.scala                     //计算大学推荐
│      
├─function
│      Order.scala                      //按秩序值排序
│      README-Algorithms.md             //算法说明
│      RecommendationAlgorithm.scala    //IER算法实现
│      RecommendationAlgorithm_1.scala  //全部相似度算法,本系统选用了多种相似度算法,通过对比评估提出一种比较好的算法:IER
│      ResultEvaluation.scala           //算法结果聘雇方法
│      
└─update
        UpdateRecommendation.scala      //更新接口
```
2. common
```
    GetMatrix.scala                     //从mysql获取评分矩阵
    GetSchema.scala                     //创建Spark生成dataframe需要的schema
```
3. dbprocess
```
    CollegeMajorRating.scala            //处理学科评估分数
    McodeSet.scala                      //处理学科专业对照
```
4. service
```
│  DataReader.scala                     //sparksql读数据
│  DataWriter.scala                     //sparksql写数据
│  MysqlService.scala                   //mysql读写服务
│  SparkService.scala                   //创建spark程序入口
│  
└─impl                                  //对应实现类
        DataReaderImpl.scala
        DataWriterImpl.scala
        MysqlServiceImpl.scala
        SparkServiceImpl.scala
```
5. util
```
    GetLogger.scala                     //获取logger
    GetProperties.scala                 //从properties获取信息
```


