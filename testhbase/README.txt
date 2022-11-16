###########环境依赖
python 3.8.5
scrapy 2.5.1
pandas 1.1.5
numpy 1.19.2
happybase 1.2.0


###########目录结构描述
├── README.txt                   
├── scrapy.cfg	// 项目配置
├── testhbase			
│   ├── spiders
	├──__init__.py	
	├── habase.py	//spider文件，爬虫功能模块（解析、过滤）
│   ├──__init__.py
│   ├──main.py		//爬虫控制运行文件
│   ├── items.py		//定义数据结构
│   ├── middlewares.py	//项目中间件，自定义丰富功能
│   ├── pipelines.py	//管道文件
│   ├── settings.py		//配置项目的设置
│   ├── useful_school.xlsx	//大学基本信息表格
│   ├── search.py		//自写python访问HBase
│   ├── scrapy_run.log	//爬虫运行日志


###########项目功能描述
该scrapy项目实现了对中国大学的官网网页数据抓取，解析，过滤和持久化。
爬虫模块有控制模块，功能模块，数据输出模块。
main.py实现对爬虫模块的控制以及日志文件的编写
spiders.py实现对数据的爬取，解析，过滤，并将数据传递给pipeline
pipeline.py实现与HBase数据库的链接和数据的持久化


