import win32com.client
import threading
from bluetooth import *
from controllerInterface import *
import socket
import pyperclip
import pptInterface
import pythoncom
import shutil
uuid = "6f5b3d7b-fb44-46f0-8120-043ee4e12261"
uuidClient = "af83dc63-c212-49c4-99d5-c91782900315"

KEY_PRESS = 0x3A
KEY_RELEASE = 0x3B
TOUCHPAD_PRESSED = 0x0E
COORDINATES = 0x07
MSG_START = 0x0F
NEXT_SLIDE = 0x11
PREV_SLIDE = 0x10
SLIDE_SHOW = 0x12
GO_TO_SLIDE = 0x13
GET_NOTE = 0x14
SYNCHRONIZE = 0x15

NOTE = 0x01
NUM_SLIDES = 0x02
CURRENT_SLIDE_IMG = 0x03
SLIDE_IMG = 0x04
CURRENT_SLIDE = 0x05
SYNC_FINISH = 0x06
SYNC_ACK = 0x07
SYNC_FAIL = 0x08

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)


class ConnectThread (threading.Thread):
    def __init__(self, threadID, name, gui):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.exitFlag = 0
        self.gui = gui
    def run(self):
        #print "Starting " + self.name
        connector(self,self.name,self.gui)
        self.gui.updateStatus("Server off")
        #print "Exiting " + self.name
    def cancel(self):
        self.exitFlag = 1
        try:
            self.server_sock.close()
        except:
            pass
        try:
            self.client_sock.close()
        except:
            pass

def getWord(socket):
    w = ord(socket.recv(1))
    w = (ord(socket.recv(1))<<8) + w
    return w

def getMultiplier(v,vprev,c):
    change = abs(abs(v) - abs(vprev))
    if abs(v)>abs(vprev):
        return min(c+(change/(abs(vprev)+1)),5)
    elif abs(v)<abs(vprev):
        c-=(change/(abs(vprev)+1))
        return max(c,1)
    else:
        return 1

def getPointerDisplacement(d):
    D = pow(abs(d),1.2)
    if d<0:
        D *= (-1)
    return D
    
def sendNote(slide,recAddr):
    if(slide!=None):
        note = pptInterface.getNote(slide).encode('utf-8')
        byteLength = len(note)
        if(note!=None):
            service_matches = find_service( uuid = uuidClient, address = recAddr )
            if len(service_matches) == 0:
                #print "couldn't find the FooBar service"
                pass
            else:
                match = service_matches[0]
                client_port = match["port"]
                client_name = match["name"]
                client_host = match["host"]
                #print client_host,client_port
                sock=BluetoothSocket( RFCOMM )
                sock.connect((recAddr, client_port))
                sock.send(chr(NOTE))
                #print 'bytelength:',byteLength
                sock.send(intToBytes(byteLength,4))
                sock.send(note)
                sock.close()

def sendImg(slide,recAddr,msgHeader):
    if(slide!=None):
        service_matches = find_service( uuid = uuidClient, address = recAddr )
        if len(service_matches) == 0:
            #print "couldn't find the FooBar service"
            pass
        else:
            match = service_matches[0]
            client_port = match["port"]
            client_name = match["name"]
            client_host = match["host"]
            print client_host,client_port
            sock=BluetoothSocket( RFCOMM )
            sock.connect((recAddr, client_port))
            slide.export(resource_path("currentSlide.jpg"),"JPG",360,270)
            byteLength = os.path.getsize(resource_path("currentSlide.jpg"))
            sock.send(chr(msgHeader))
            sock.send(intToBytes(byteLength,4))
            f = open(resource_path("currentSlide.jpg"),'rb')
            l = f.read(1024)
            while (l):
                #print 'Sending...'
                sock.send(l)
                l = f.read(1024)
            f.close()
            sock.close()

def sendImage(path,sock):
    byteLength = os.path.getsize(path)
    sock.send(intToBytes(byteLength,4))
    f = open(path,'rb')
    l = f.read(1024)
    while (l):
        #print 'Sending...'
        sock.send(l)
        l = f.read(1024)
    f.close()

def sendCurrentSlideIndex(slide,recAddr):
    if slide==None: return
    service_matches = find_service( uuid = uuidClient, address = recAddr )
    if len(service_matches) == 0:
        #print "couldn't find the FooBar service"
        pass
    else:
        match = service_matches[0]
        client_port = match["port"]
        client_name = match["name"]
        client_host = match["host"]
        #print client_host,client_port
        sock=BluetoothSocket( RFCOMM )
        sock.connect((recAddr, client_port))
        sendCurrentSlideNum(slide.SlideIndex,sock)
        sock.close()

def sendCurrentSlideNum(num,sock):
    sock.send(chr(CURRENT_SLIDE))
    sock.send(intToBytes(num,2))

def synchronize(recAddr):
        service_matches = find_service( uuid = uuidClient, address = recAddr )
        if len(service_matches) == 0:
            #print "couldn't find the FooBar service"
            pass
        else:
            match = service_matches[0]
            client_port = match["port"]
            client_name = match["name"]
            client_host = match["host"]
            #print client_host,client_port
            sock=BluetoothSocket( RFCOMM )
            sock.connect((recAddr, client_port))
            sock.send(chr(SYNC_ACK))
            numSlides,notes,curSlideNum = pptInterface.sync()
            #print 'numSlides',numSlides
            #print 'curSlideNum',curSlideNum
            if numSlides!=None:
                #print 'sync started'
                sock.send(chr(NUM_SLIDES))
                sock.send(intToBytes(numSlides,2))
                sock.send(chr(CURRENT_SLIDE))
                sock.send(intToBytes(curSlideNum,2))
                for i in xrange(numSlides):
                    index = i+1
                    sock.send(chr(SLIDE_IMG))
                    sock.send(intToBytes(index,2))
                    path = "slidepics\\Slide"
                    path += str(index)
                    path += ".jpg"
                    sendImage(path,sock)
                    if notes[i]!=None:
                        sock.send(chr(NOTE))
                        sock.send(intToBytes(index,2))
                        note = notes[i].encode('utf-8')
                        sock.send(intToBytes(len(note),4))
                        sock.send(note)
                sock.send(chr(SYNC_FINISH))
            else:
                sock.send(chr(SYNC_FAIL))
            sock.close()
            try:
                shutil.rmtree("slidepics")
            except:
                pass
                        

def getByte(num,offset):
    if(num<0):
        return None
    index = offset + 7
    ans = 0
    while(index>=offset):
        ans = (ans<<1) + ((num>>index)%2)
        index -= 1
    return ans

def intToBytes(num,noOfBytes):
    offset = 0
    byteString = str()
    for i in xrange(noOfBytes):
        byteString += chr(getByte(num,offset))
        offset += 8
    return byteString
        

def connector(thread,threadName,gui):
    pythoncom.CoInitialize()
    loop = True
    while loop:
        if thread.exitFlag:
            return
        try:
            server_sock=BluetoothSocket( RFCOMM )
            server_sock.bind(("",PORT_ANY))
            server_sock.listen(1)
            port = server_sock.getsockname()[1]
            advertise_service( server_sock, "pptController",
                               service_id = uuid,
                               service_classes = [ uuid, SERIAL_PORT_CLASS ],
                               profiles = [ SERIAL_PORT_PROFILE ], 
            #                   protocols = [ OBEX_UUID ] 
                                )
            #print 'bluetooth server_sock created'
            thread.server_sock = server_sock
            loop = False
        except:
            gui.updateStatus("Unable to start server (Make sure bluetooth is on)")
            loop = True
    keysPressed = {}
    xCord = 0
    yCord = 0
    while(thread.exitFlag==0):
        #print "waiting for connection.."
        try:
            gui.updateStatus("Waiting for client to connect")
            client_sock, client_info = server_sock.accept()
            #print("Accepted connection from ", client_info,"type",type(client_info),"name",client_sock.getsockname())
            gui.updateStatus("Connected to " + lookup_name(client_info[0]))
            thread.client_sock = client_sock
        except:
            pass
            #print 'error accepting'
##        try:
        while thread.exitFlag==0:
            command = client_sock.recv(1)
            if len(command) == 0: break
            cmd = ord(command)
            if cmd==0 or cmd==255:
                continue
            elif cmd==TOUCHPAD_PRESSED:      #mouse move starts
                xCord = getWord(client_sock)
                yCord = getWord(client_sock)
            elif cmd==COORDINATES:      #mouse coordinates
                x = getWord(client_sock)
                y = getWord(client_sock)
                dx = (x - xCord)
                dy = (y - yCord)
                MoveMouse(dx,dy)
                xCord = x
                yCord = y
            elif cmd==KEY_PRESS:
                key = client_sock.recv(1)
                PressKey(ord(key))
                keysPressed[key] = None
            elif cmd==KEY_RELEASE:
                key = client_sock.recv(1)
                if key in keysPressed:
                    ReleaseKey(ord(key))
                    del keysPressed[key]
            elif cmd==MSG_START:
                msgLen = getWord(client_sock)
                bytesReceived = ''
                while(len(bytesReceived)<msgLen):
                    bytesReceived += client_sock.recv(msgLen-len(bytesReceived))
                msg = bytesReceived.decode('utf-8')
                pyperclip.copy(msg)
                paste()
            elif cmd==PREV_SLIDE:
                #print 'prev'
                slide = pptInterface.prev()
                #print pptInterface.getNote(slide)
                sendCurrentSlideIndex(slide,client_info[0])     
            elif cmd==NEXT_SLIDE:
                #print 'next'
                slide = pptInterface.next()
                #print pptInterface.getNote(slide)
                sendCurrentSlideIndex(slide,client_info[0]) 
            elif cmd==SLIDE_SHOW:
                slide = pptInterface.startSlideShow()
                sendCurrentSlideIndex(slide,client_info[0]) 
            elif cmd==GO_TO_SLIDE:
                index = getWord(client_sock)
                slide = pptInterface.goToSlide(index)
                sendCurrentSlideIndex(slide,client_info[0])
            elif cmd==SYNCHRONIZE:
                synchronize(client_info[0])
##            elif cmd==GET_NOTE:
##                sendNote(pptInterface.getCurrentSlide(),client_info[0],CURRENT_SLIDE_IMG)
##        except IOError:
##            pass
        for key in keysPressed:
            ReleaseKey(ord(key))
        keysPressed = {}
        #print("disconnected")
        try:
            client_sock.close()
        except:
            pass
    try:
        server_sock.close()
    except:
        pass
                
