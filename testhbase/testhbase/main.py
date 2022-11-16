#   -*- codeing = utf-8 -*-
#   @Time : 2022/3/7 21:05
#   @Author : Xiong
#   @File : main.py
#   @Software : PyCharm
import logging
import time
import os

while True:
    time_now=time.strftime("%H:%M",time.localtime())
    if time_now=='16:00':   # 每天运行爬虫的时间
        # 设置日志格式
        LOG_FORMAT = "%(asctime)s - %(levelname)s - %(message)s"
        # 设置日期格式
        DATE_FORMAT = "%Y/%m/%d %H:%M:%S %p"
        logging.basicConfig(level=logging.DEBUG,
                            filename=r'E:\python\testhbase\testhbase\scrapy_run.log', filemode='a',
                            format=LOG_FORMAT, datefmt=DATE_FORMAT)
        logging.info('The crawler is running now !')
        start_time = time.time()
        os.system("scrapy crawl habase")
        stop_time=time.time()
        logging.info('The crawler finished running and took %d seconds !'%(stop_time-start_time))
        # print('本轮爬虫运行时间为：%d秒'%(stop_time-start_time))
    time.sleep(10)