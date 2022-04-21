import datetime
import tkinter as tk
from turtle import clear
import Threads
import socket
import string
import time
import threading
from threading import Thread, main_thread
from urllib.request import urlopen
from json import load
from tkinter import END, messagebox
import os
from tkinter import scrolledtext
import inspect
import ctypes
import sys
import json
import tkinter as tk
from tkinter import ttk
from tkinter import * 


my_ip=' '.encode('utf_8')
#my_ip = urlopen('http://ip.42.pl/raw').read()
Token=None
Host=None
Port=None
MYADDR=(('0.0.0.0',30000))
ADDR=(('36.40.74.162',20000))
#1.15.25.83
#36.40.74.162
#ADDR=MYADDR

id=None
BUFFSIZE=None

class window_cache:
    id_input=None
    psw_input=None
    message_box=None
    friend_list=None
    apply_recieved=None

class my_info:
    nickname=None
    cache=[]
    id=None
    Token=None
    password=None
    MYADDR=(('0.0.0.0',30000))
    ADDR=(('36.40.74.162',20000))
    def __init__(self,cache,id,Token,ADDR,MYADDR):
        self.cache=cache
        self.id=id
        self.Token=Token
        self.MYADDR=MYADDR
        self.ADDR=ADDR
    def set_id(self,id):
        self.id=id
    def set_nickname(self,nickname):
        self.nickname=nickname
    def set_password(self,password):
        self.password=password

window_item=window_cache()
info=my_info(None,None,None,ADDR,MYADDR)
info.set_nickname('我')
def GUI_Change_Password(cache,ADDR,s):
    change_password_window=tk.Tk()
    change_password_window.title('更改密码')
    change_password_window.geometry('300x200')
    lable1=tk.Label(change_password_window,text='账号',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    lable2=tk.Label(change_password_window,text='原密码',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    lable3=tk.Label(change_password_window,text='新密码',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    entry_id=tk.Entry(change_password_window,bg='white',fg='blue',relief=tk.RAISED,width=20)
    entry_password=tk.Entry(change_password_window,bg='white',fg='blue',relief=tk.RAISED,width=20,show='*')
    entry_newpassword=tk.Entry(change_password_window,bg='white',fg='blue',relief=tk.RAISED,width=20)
    change_password_button=tk.Button(change_password_window,text='更改密码',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED,command=lambda:[Threads.Start_Change_Password_Thread(cache,entry_id.get(),entry_password.get(),entry_newpassword.get(),ADDR,s),change_password_window.quit()])

    lable1.place(rely=0.1)
    lable2.place(rely=0.35)
    lable3.place(rely=0.6)

    entry_id.place(relx=0.2,rely=0.1)
    entry_password.place(relx=0.2,rely=0.35)
    entry_newpassword.place(relx=0.2,rely=0.6)
    change_password_button.place(rely=0.8,relx=0.7)
    change_password_window.mainloop()
    return change_password_window

def GUI_Signup(cache,ADDR,s):
    signup_window=tk.Tk()
    signup_window.title('注册')
    signup_window.geometry('300x200')
    lable1=tk.Label(signup_window,text='账号',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    lable2=tk.Label(signup_window,text='密码',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    lable3=tk.Label(signup_window,text='昵称',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    entry_id=tk.Entry(signup_window,bg='white',fg='blue',relief=tk.RAISED,width=20)
    entry_password=tk.Entry(signup_window,bg='white',fg='blue',relief=tk.RAISED,width=20,show='*')
    entry_nickname=tk.Entry(signup_window,bg='white',fg='blue',relief=tk.RAISED,width=20)
    signup_button=tk.Button(signup_window,text='注册',bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED,command=lambda:[Threads.Start_Signup_Thread(cache,entry_id.get(),entry_nickname.get(),entry_password.get(),ADDR,s),signup_window.destroy()])
    lable1.place(rely=0.1)
    lable2.place(rely=0.35)
    lable3.place(rely=0.6)
    entry_id.place(relx=0.2,rely=0.1)
    entry_password.place(relx=0.2,rely=0.35)
    entry_nickname.place(relx=0.2,rely=0.6)
    signup_button.place(rely=0.8,relx=0.7)
    signup_window.mainloop()
    return signup_window

def signup(cache,ADDR,s):
    Current_window=GUI_Signup(cache,ADDR,s)

def GUI_Login(cache:window_cache,info,ADDR,s):

    login_window = Tk()
    # This is the section of code which creates the main window
    login_window.geometry('340x200')
    login_window.configure(background='#EED5B7')
    login_window.title('登录')
    # This is the section of code which creates a text input box
    id_input=Entry(login_window)
    id_input.place(x=149, y=25)
    # This is the section of code which creates a text input box
    psw_input=Entry(login_window,show='*')
    psw_input.place(x=149, y=85)
    # This is the section of code which creates the a label
    Label(login_window, text='账号', bg='#EED5B7', font=('arial', 12, 'normal')).place(x=89, y=25)
    # This is the section of code which creates the a label
    Label(login_window, text='密码', bg='#EED5B7', font=('arial', 12, 'normal')).place(x=89, y=85)
    # This is the section of code which creates a button
    Button(login_window, text='登录', bg='#838B8B', font=('arial', 12, 'normal'),command=lambda:[Threads.Start_Login_Thread(cache,id_input.get(),psw_input.get(),info,ADDR,s),info.set_id(id_input.get()),info.set_password(psw_input.get()),login_window.destroy()]).place(x=149, y=145)
    # This is the section of code which creates a button
    Button(login_window, text='更改密码', bg='#838B8B', font=('arial', 12, 'normal'),command=lambda:[GUI_Change_Password(cache,ADDR,s),login_window.destroy()]).place(x=239, y=145)
    # This is the section of code which creates a button
    Button(login_window, text='注册', bg='#838B8B', font=('arial', 12, 'normal'),command=lambda:[signup(cache,ADDR,s),login_window.destroy()]).place(x=59, y=145)
    login_window.mainloop()
    return login_window


def login(cache,info:my_info,ADDR,s):
    Current_window=GUI_Login(cache,info,ADDR,s)
    time.sleep(1)
    print(info.Token,'login')
    if info.Token==None:
        try:
            Current_window.destroy()
        except:
            pass
        next_choose=messagebox.askretrycancel('提示','登陆失败')
        if next_choose==True:
            login(cache,info,ADDR,s)
        if next_choose==False:
            time.sleep(3)
            os._exit(0)
            
    else:
        try:
            Current_window.destroy()
        except:
            pass
        return 1
        
def show_info(str,nickname,entry_message,message_box_show:window_cache,time=datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")):
    s_time = time
    str = str.rstrip()
    if len(str) == 0:
        return -1
    entry_message.delete(0,END)
    temp = nickname+s_time + "\n    " + str + "\n"
    message_box_show.message_box.insert(tk.INSERT, "%s" % temp)
    message_box_show.message_box.see(END)

s=socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
s.bind(MYADDR)
the_main_thread=threading.current_thread()
R_T=Threads.Start_Recieve_Thread(the_main_thread,info,id,None,window_item,s)
is_logged=login(None,info,ADDR,s)
#GUI_Login(cache,Token,ADDR,s)
print(info.Token)
if info.Token:
    id=info.id
    Token=info.Token
    heartbeat=threading.Thread(target=Threads.Start_Heart_Beat_Thread,args=(R_T,info.Token,info.id,ADDR,s),name='心跳线程',daemon=True)
    heartbeat.start()
    #Threads.Start_Heart_Beat_Thread(cache,info.Token,info.id,ADDR,s)
    root = Tk()
    # This is the section of code which creates the main window

    root.bind('<KeyPress>',lambda event:[Threads.Start_Send_Thread(None,msg_input.get(),info.id,info.Token,frd_list.get().split(' ')[-1],ADDR,s),show_info(msg_input.get(),info.nickname,msg_input,window_item)] if event.char=='\r' else None)
    
    root.geometry('780x510')
    root.configure(background='#F0F8FF')
    root.title('client of onlychat')
    # This is the section of code which creates a combo box
    frd_list= ttk.Combobox(root, values=[], font=('arial', 12, 'normal'), width=30)
    frd_list.place(x=284, y=412)
    #comboOneTwoPunch.current(1)
    # This is the section of code which creates a text input box
    msg_input=Entry(root)
    msg_input.place(x=284, y=442)



    # This is the section of code which creates a text input box
    my_apply=Entry(root)
    my_apply.place(x=14, y=122)
    # This is the section of code which creates the a label
    Label(root, text='添加好友', bg='#F0F8FF', font=('arial', 12, 'normal')).place(x=14, y=102)
    # This is the section of code which creates a button
    Button(root, text='发送申请', bg='#EEDFCC', font=('arial', 12, 'normal'), command=lambda:[Threads.Start_Add_Friend_Thread(None,info.Token,info.id,my_apply.get(),ADDR,s),my_apply.delete(0,END)]).place(x=164, y=112)
    # This is the section of code which creates a text input box
    recieve_apply=ttk.Combobox(root,values=[], font=('arial', 12, 'normal'), width=20)
    recieve_apply.place(x=14, y=212)
    # This is the section of code which creates the a label
    Label(root, text='好友申请', bg='#F0F8FF', font=('arial', 12, 'normal')).place(x=14, y=192)
    # This is the section of code which creates a button
    Button(root, text='接受', bg='#EEDFCC', font=('arial', 12, 'normal'), command=lambda:[Threads.Start_Agree_Add_Thread(window_item,info.Token,info.id,recieve_apply.get(),ADDR,s),recieve_apply.delete(0,END)]).place(x=14, y=242)
    # This is the section of code which creates a button
    Button(root, text='拒绝', bg='#EEDFCC', font=('arial', 12, 'normal'), command=lambda:[Threads.Start_Refuse_Add_Thread(window_item,info.Token,info.id,recieve_apply.get(),ADDR,s),recieve_apply.delete(0,END)]).place(x=84, y=242)



    # This is the section of code which creates a button
    Button(root, text='发送', bg='#EEDFCC', font=('arial', 12, 'normal'), command=lambda:[Threads.Start_Send_Thread(None,msg_input.get(),info.id,info.Token,frd_list.get().split(' ')[-1],ADDR,s),show_info(msg_input.get(),info.nickname,msg_input,window_item)]).place(x=714, y=442)
    # This is the section of code which creates a button
    Button(root, text='选择', bg='#EEDFCC', font=('arial', 12, 'normal')).place(x=714, y=402)
    # This is the section of code which creates the a label
    Label(root, text='本机IP为'+str(my_ip.decode('utf_8')), bg='#F0F8FF', font=('arial', 12, 'normal')).place(x=24, y=2)
    # This is the section of code which creates the a label
    Label(root, text='我的ID为'+str(info.id), bg='#F0F8FF', font=('arial', 12, 'normal')).place(x=24, y=32)
    # This is the section of code which creates the a label
    Label(root, text='我的昵称'+str(info.nickname), bg='#F0F8FF', font=('arial', 12, 'normal')).place(x=24, y=62)
    # This is the section of code which creates a text input box
    id_input=Entry(root)
    id_input.place(x=0, y=412)
    #Threads.Start_Login_Thread(cache,info.id,info.password,info,ADDR,s)
    message_box_show=scrolledtext.ScrolledText(root,bg='white',fg='blue',font=('微软雅黑',10),relief=tk.RAISED)
    message_box_show.place(width=480,height=380,x=284,y=12)
    window_item.message_box=message_box_show
    window_item.friend_list=frd_list
    window_item.apply_recieved=recieve_apply
    def on_closing():
        if messagebox.askokcancel("Quit","Do you want to quit?"):
            data=json.dumps({"ACT":"LOGOUT","ID":id,"TOKEN":Token,"TIME":time.time()}).encode('utf_8')
            print(s.sendto(data,ADDR))
            print(data)
            root.destroy()
    root.protocol("WM_DELETE_WINDOW",on_closing)
    Threads.Start_Get_Info_All_Thread(R_T,info.Token,info.id,ADDR,s)
    root.mainloop()