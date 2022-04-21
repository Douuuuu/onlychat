
import datetime
import socket
import string
import time
import json
import tkinter as tk
from tkinter import END, messagebox
def show_info(str,nickname,entry_message,message_box_show,time=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")):
    s_time = time
    str = str.rstrip()
    if len(str) == 0:
        return -1 
    temp ='                                             '+ nickname+s_time + "\n                                                " + str + "\n"
    message_box_show.message_box.insert(tk.INSERT, "%s" % temp)

def refresh_apply_tuple(apply_window,item_to_delete):
    print(apply_window['value'],'---',item_to_delete)
    index=apply_window['value'].index(item_to_delete)
    if index==len(apply_window['value'])-1:
        refreshed_tuple=apply_window['value'][:-1]
    else:
        refreshed_tuple=apply_window['value'][:index]+apply_window['value'][index+1:]
    apply_window['value']=refreshed_tuple
    print(apply_window['value'])

Token=None
Host=None
Port=None
ADDR=(Host,Port)
cache=[]
id=None
BUFFSIZE=None
#{"ACT":"SEND","TOKEN":"token","ID":"id","TARGET_ID":"target id","MSG":"message","TIME":"time"}
def send(cache,msg:string,id:string,Token:string,target_id:string,ADDR,s:socket):
    data = json.dumps({"ACT":"SEND","TOKEN":Token,"ID":id,"TARGET_ID":target_id,"MSG":msg,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))
    print(data)

def signup(cache,id:string,name:string,password:string,ADDR,s:socket):
    data = json.dumps({"ACT":"SIGNUP","ID":id,"NAME":name,"PASSWD":password,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))
    print(data)

def change_password(cache,id:string,old_password:string,new_password:string,ADDR,s):
    data = json.dumps({"ACT":"CHANGE_PASSWD","ID":id,"PASSWD":old_password,"NEW_PASSWD":new_password,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))
    print(data)

def login(cache,id:string,password:string,ADDR,s):
    data = json.dumps({"ACT":"LOGIN","ID":id,"PASSWD":password,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))
    print(data)

def add_friend(cache,token:string,id:string,target_id:string,ADDR,s):
    data = json.dumps({"ACT":"ADD_FRIEND","ID":id,"TOKEN":token,"TARGET_ID":target_id,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))

def agree_add(cache,token:string,id:string,target_id:string,ADDR,s):
    data = json.dumps({"ACT":"AGREE_ADD","ID":id,"TOKEN":token,"TARGET_ID":target_id,"TIME":time.time()}).encode('utf_8')
    s.sendto(data,ADDR)
    refresh_apply_tuple(cache.apply_recieved,target_id)

def refuse_add(cache,token:string,id:string,target_id:string,ADDR,s):
    data = json.dumps({"ACT":"REFUSE_ADD","ID":id,"TOKEN":token,"TARGET_ID":target_id,"TIME":time.time()}).encode('utf_8')
    s.sendto(data,ADDR)
    refresh_apply_tuple(cache.apply_recieved,target_id)

def get_info_all(cache,token:string,id:string,ADDR,s):
    data = json.dumps({"ACT":"GET_INFO_ALL","ID":id,"TOKEN":token,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))
    print(data)

def get_info(cache,token:string,id:string,target_id:string,ADDR,s):
    data = json.dumps({"ACT":"GET_INFO","ID":id,"TARGET_ID":target_id,"TOKEN":token,"TIME":time.time()}).encode('utf_8')
    print(s.sendto(data,ADDR))

def heart_beat(cache,token:string,id:string,ADDR,s):
    data = json.dumps({"ACT":"HEART_BEAT","ID":id,"TOKEN":token,"TIME":time.time()}).encode('utf_8')
    s.sendto(data,ADDR)

#缺少ADDR
def recieve(cache,info,id,entry_message,message_box_show,s,BUFFSIZE=1024):
    reasonlist=['','','ID存在','密码不符合要求','ID不符合要求','昵称不符合要求','','','','ID不存在']
    while True:
        print('接收中')
        encode_data, address = s.recvfrom(BUFFSIZE)
        print('接收到数据',encode_data.decode('utf_8'))
        decode_data=encode_data.decode('utf_8')
        null=None
        NULL=None
        data=eval(decode_data)
        #data=json.loads(encode_data.decode('utf_8'))
        #{"ACT":"RECEIVE","SEND_ID":"id","ID","id","MSG":"message","MSG_ID":"message id","MSG_SEND_TIME":"messgae send time","TIME":"time"}
        if data["ACT"]=="RECEIVE":
            show_info(data['MSG'],data['SEND_NAME'],entry_message,message_box_show,time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(float(int(data['MSG_SEND_TIME']))/1000)))
        elif data["ACT"]=="LOGIN_SUCCESS":
            #messagebox.showinfo('登陆成功')
            info.Token=data["TOKEN"]
            info.nickname=data['NAME']
            print('登陆成功',info.Token)
        elif data["ACT"]=="SIGNUP_SUCCESS":
            try:
                messagebox.showinfo('注册成功','ID:'+str(data['ID']))
            except:
                pass
        elif data["ACT"]=="INFO_ALL":
            message_box_show.friend_list.delete(0,END)
            list=[item['NAME']+' '+item['STATUS']+' '+item['ID'] for item in eval(data['LIST'])]
            message_box_show.friend_list['value']=tuple(list)
        elif data["ACT"]=="SIGNUP_FAILED":
            #reasonlist=['','','ID存在','密码不符合要求','ID不符合要求','昵称不符合要求']
            #messagebox.showinfo('注册失败',reasonlist[int(data['REASON'])])
            pass
        elif data["ACT"]=="CHANGE_SUCCESS":
            messagebox.showinfo('改密成功','改密成功')
            time.sleep(2)
        elif data["ACT"]=="CHANGE_FAILED":
            #reasonlist=['密码未变化','不符合要求','密码错误']
            messagebox.showinfo('改密失败',reasonlist[int(data['REASON'])])
            time.sleep(3)
            pass    
        elif data["ACT"]=="LOGIN_FAILED":
            #reasonlist=['ID不存在','密码账户不匹配']
            #messagebox.showinfo('登陆失败',reasonlist[int(data['REASON'])])
            pass
        elif data["ACT"]=="ADD_SUCCESS":
            data = json.dumps({"ACT":"GET_INFO_ALL","ID":info.id,"TOKEN":info.Token,"TIME":time.time()}).encode('utf_8')
            print(s.sendto(data,info.ADDR))
            #print('添加ID为',data["ID"],'的好友成功')
        elif data["ACT"]=="ADD_FAILED":
            #print('添加ID为',data["ID"],'的好友失败')
            pass
        #添加好友转发 {"ACT":"FRIEND_APPLY","ID":"id","TIME":"time"}
        elif data["ACT"]=="FRIEND_APPLY":
            #message_box_show.friend_list['value']=tuple(list)
            new=tuple([data['ID']])
            apply_tuple=new + tuple(message_box_show.apply_recieved['value'])
            message_box_show.apply_recieved['value']=apply_tuple

            '''message_box_show.apply_recieved.delete(0,END)
            message_box_show.apply_recieved.insert(0,data['ID'])'''
        elif data['ACT']=="":
            get_info_all(cache,info.Token,info.id,ADDR,s)
        '''if data['ACT']!="SEND_SUCCESS":
            success_reply={"ACT":"RECEIVE_SUCCESS","MSG_ID":data["MSG_ID"],"TIME":time.time()}
            print(s.sendto(success_reply,address,s))'''
    s.close()
