# -*- mode: python -*-

block_cipher = None


a = Analysis(['app.py'],
             pathex=['c:\\remoteControllerServer\\app'],
             binaries=[],
             datas=[],
             hiddenimports=[],
             hookspath=[],
             runtime_hooks=[],
             excludes=[],
             win_no_prefer_redirects=False,
             win_private_assemblies=False,
             cipher=block_cipher)
a.datas += [('remotecontrol.png', 'c:\\remoteControllerServer\\app\\remotecontrol.png',  'DATA'),('icon.ico', 'c:\\remoteControllerServer\\app\\icon.ico',  'DATA')]
pyz = PYZ(a.pure, a.zipped_data,
             cipher=block_cipher)
exe = EXE(pyz,
          a.scripts,
          a.binaries,
          a.zipfiles,
          a.datas,
          name='app',
          debug=False,
          strip=False,
          upx=True,
          console=True , icon='icon.ico')
