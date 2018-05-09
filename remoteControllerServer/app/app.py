import Tkinter
from Tkinter import *
from PIL import Image,ImageTk
from connection import *
button_font = "calibri"

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

class App:
    def __init__(self, master):
        self.frame = Frame(master,bg="white")
        self.frame.pack()
        remote_photo = ImageTk.PhotoImage(Image.open(resource_path("remotecontrol.png")))
        self.remote_label = Label(self.frame,image=remote_photo,bd=0)
        self.remote_label.image = remote_photo
        self.remote_label.pack(side=TOP)
        self.start_button = Button(
            self.frame, text="Start Server", command=self.start, bg="dodger blue", fg="white",font = button_font,width=12
            )
        self.start_button.pack(side=TOP,pady=10)
        self.stop_button = Button(
            self.frame, text="Stop Server", command=self.stop,bg="red",fg="white",font = button_font,width=12
            )
        self.stop_button.pack(side=TOP)
        self.status= Label(self.frame, text="Status: Server off",bg = "white",font = button_font)
        self.status.pack(side=TOP,pady=5)
        self.serverOn = False
        self.connectThread = None

    def start(self):
        if(self.serverOn == False):
            self.serverOn = True
            self.connectThread = ConnectThread(1,"Bluetooth connect thread",self)
            self.connectThread.start()

    def stop(self):
        self.serverOn = False
        try:
            self.connectThread.cancel()
        except:
            pass

    def updateStatus(self,status):
        status = "Status: " + status
        self.status.config(text=status)




root = Tk()
root.title("Remote Controller Server - By Neeraj Kerkar")
app = App(root)

def on_closing():
    app.stop()
    root.destroy()
    
root.protocol("WM_DELETE_WINDOW", on_closing)
root.mainloop()
