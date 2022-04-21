import string
import time
import threading

import client

Token=None
Host=None
Port=None
MYADDR=(('0.0.0.0',20000))
ADDR=(('1.15.25.83',20000))
#ADDR=MYADDR
id=None
BUFFSIZE=None

def refresh_apply_tuple(apply_tuple:tuple,item_to_delete):
    print(apply_tuple,'---',item_to_delete)
    index=apply_tuple.index(item_to_delete)
    if index==len(apply_tuple)-1:
        refreshed_tuple=apply_tuple[:-1]
    else:
        refreshed_tuple=apply_tuple[:index]+apply_tuple[index+1:]
    apply_tuple=refreshed_tuple
    print(apply_tuple)

def Start_Recieve_Thread(cache,info,id,entry_message,message_box_show,s):
    Recieve_Thread=threading.Thread(target=client.recieve,args=(cache,info,id,entry_message,message_box_show,s),name='接收线程')
    Recieve_Thread.setDaemon(True)
    Recieve_Thread.start()
    return Recieve_Thread

def Start_Send_Thread(cache,msg,id,Token,target_id,ADDR,s):
    Send_Thread=threading.Thread(target=client.send,args=(cache,msg,id,Token,target_id,ADDR,s))
    Send_Thread.start()

def Start_Signup_Thread(cache,id:string,name:string,password:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.signup,args=(cache,id,name,password,ADDR,s))
    Send_Thread.start()

def Start_Change_Password_Thread(cache,id:string,old_password:string,new_password:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.change_password,args=(cache,id,old_password,new_password,ADDR,s))
    Send_Thread.start()

def Start_Login_Thread(cache,id:string,password:string,info,ADDR,s):
    info.id=id
    Send_Thread=threading.Thread(target=client.login,args=(cache,id,password,ADDR,s))
    Send_Thread.start()

def Start_Add_Friend_Thread(cache,token:string,id:string,target_id:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.add_friend,args=(cache,token,id,target_id,ADDR,s))
    Send_Thread.start()

def Start_Agree_Add_Thread(cache,token:string,id:string,target_id:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.agree_add,args=(cache,token,id,target_id,ADDR,s))
    Send_Thread.start()

def Start_Refuse_Add_Thread(cache,token:string,id:string,target_id:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.refuse_add,args=(cache,token,id,target_id,ADDR,s))
    Send_Thread.start()

def Start_Get_Info_Thread(cache,token:string,id:string,target_id:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.get_info,args=(cache,token,id,target_id,ADDR,s))
    Send_Thread.start()

def Start_Get_Info_All_Thread(cache,token:string,id:string,ADDR,s):
    Send_Thread=threading.Thread(target=client.get_info_all,args=(cache,token,id,ADDR,s))
    Send_Thread.start()

def Start_Heart_Beat_Thread(cache,token:string,id:string,ADDR,s):
    while cache.is_alive():
        Send_Thread=threading.Thread(target=client.heart_beat,args=(cache,token,id,ADDR,s))
        Send_Thread.setDaemon(True)
        Send_Thread.start()
        time.sleep(2)