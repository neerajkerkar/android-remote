import win32com.client
import shutil
import os

msoPlaceholder = 14
ppPlaceholderBody = 2

def resource_path(relative_path):
    """ Get absolute path to resource, works for dev and for PyInstaller """
    try:
        # PyInstaller creates a temp folder and stores path in _MEIPASS
        base_path = sys._MEIPASS
    except Exception:
        base_path = os.path.abspath(".")

    return os.path.join(base_path, relative_path)

def notesTextPlaceholder(slide):
    for shape in slide.NotesPage.Shapes:
        if shape.Type == msoPlaceholder:
            if shape.PlaceholderFormat.Type == ppPlaceholderBody:
                return shape
    return None

def printAllNotes():
    Application = win32com.client.GetActiveObject("PowerPoint.Application")
    print 'running instance found'
    Presentation = Application.ActivePresentation
    print 'active presentation found'
    count = 0
    for Slide in Presentation.Slides:
        count+=1
        #print 'slide',count
        shape = notesTextPlaceholder(Slide)
        if(shape!=None):
            print 'note:'
            print shape.TextFrame.TextRange.Text
        else:
            print 'no note'

def sync():
    try:
        Application = win32com.client.GetActiveObject("PowerPoint.Application")
        #print 'running instance found'
        Presentation = Application.ActivePresentation
        #print 'active presentation found'
        count = 0
        notes = []
        slides = Presentation.Slides
        try:      
            curSlide = slides[Application.ActiveWindow.Selection.SlideRange.SlideNumber-1]
        except:            
            curSlide = Application.SlideShowWindows[1].View.Slide;
        for Slide in Presentation.Slides:
            count+=1
            #print 'slide',count
            shape = notesTextPlaceholder(Slide)
            if(shape!=None):
                #print 'note:'
                notes.append(shape.TextFrame.TextRange.Text)
                #print shape.TextFrame.TextRange.Text
            else:
                notes.append(None)
                #print 'no note'
        try:
            shutil.rmtree(resource_path("slidepics"))
            #print "Successful"
        except:
            pass
        Presentation.export(resource_path("slidepics"),"JPG",360*1.2,270*1.2)
        return count,notes,curSlide.SlideIndex
    except:
        return None,None,None
            

def startSlideShow():
    try:
        pptApplication = win32com.client.GetActiveObject("PowerPoint.Application")
        presentation = pptApplication.ActivePresentation
        slides = presentation.Slides
        try:
            window = presentation.SlideShowWindow
        except:
            index = slides[pptApplication.ActiveWindow.Selection.SlideRange.SlideNumber-1].SlideIndex
            presentation.SlideShowSettings.Run()
            pptApplication.SlideShowWindows[1].View.GotoSlide(index)
            return pptApplication.SlideShowWindows[1].View.Slide
    except:
        return None
    
def goToSlide(index):
    try:
        pptApplication = win32com.client.GetActiveObject("PowerPoint.Application")
        presentation = pptApplication.ActivePresentation
        slides = presentation.Slides
        try:      
            slide = slides[index-1];
            slide.select()
        except:            
            pptApplication.SlideShowWindows[1].View.GotoSlide(index)
            slide = pptApplication.SlideShowWindows[1].View.Slide
        return slide
    except:
        return None

def getNote(slide):
    if slide==None:
        return None
    shape = notesTextPlaceholder(slide)
    if(shape!=None):
        return shape.TextFrame.TextRange.Text
    return None

def getCurrentSlide():
    try:
        pptApplication = win32com.client.GetActiveObject("PowerPoint.Application")
        presentation = pptApplication.ActivePresentation
        slides = presentation.Slides
        try:      
            slide = slides[pptApplication.ActiveWindow.Selection.SlideRange.SlideNumber-1]
        except:            
            slide = pptApplication.SlideShowWindows[1].View.Slide;
        return slide
    except:
        return None

def next():
    try:
        pptApplication = win32com.client.GetActiveObject("PowerPoint.Application")
        presentation = pptApplication.ActivePresentation
        slides = presentation.Slides
        try:      
            slide = slides[pptApplication.ActiveWindow.Selection.SlideRange.SlideNumber-1]
        except:            
            slide = pptApplication.SlideShowWindows[1].View.Slide;
        slideIndex = (slide.SlideIndex - 1) + 1
        slidesCount = slides.count
        if(slideIndex < slidesCount):
            try:
                slide = slides[slideIndex]
                slides[slideIndex].Select()
            except:
                pptApplication.SlideShowWindows[1].View.Next()
                slide = pptApplication.SlideShowWindows[1].View.Slide
        return slide
    except:
        return None

def prev():
    try:
        pptApplication = win32com.client.GetActiveObject("PowerPoint.Application")
        presentation = pptApplication.ActivePresentation
        slides = presentation.Slides
        try:      
            slide = slides[pptApplication.ActiveWindow.Selection.SlideRange.SlideNumber-1]
        except:            
            slide = pptApplication.SlideShowWindows[1].View.Slide;
        slideIndex = (slide.SlideIndex - 1) - 1
        if(slideIndex >= 0):
            try:
                slide = slides[slideIndex]
                slides[slideIndex].Select()
            except:
                pptApplication.SlideShowWindows[1].View.Previous()
                slide = pptApplication.SlideShowWindows[1].View.Slide
        return slide
    except:
        return None

def getSlideImages():
    pptApplication = win32com.client.GetActiveObject("PowerPoint.Application")
    presentation = pptApplication.ActivePresentation
    try:
        shutil.rmtree(app.resource_path("slidepics"))
    except:
        pass
    presentation.export(resource_path("slidepics"),"JPG",460,370)
            


