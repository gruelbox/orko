;Orko Installer
;Written by Keni Barwick
;with many Google searches and a bunch posts by some Stack Overflow geniuses 
;
; TODO:
;       Use varibles to remove duplication - e.g. LicenseData
;       Auto check 32 or 64 Java download url 
;       Icons
;       Splash Screen
;       BG Image
;       

!define JRE_VERSION "1.8"
; The URLs are obtained from https://www.java.com/en/download/manual.jsp
!define JRE_32_URL "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=216432"
!define JRE_64_URL "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=216434"
!define JRE_URL "http://javadl.oracle.com/webapps/download/AutoDL?BundleId=216434" ; set to 64 would need to set to correct processor 

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"
  !include "LogicLib.nsh"
  !include "JREDyna_Inetc.nsh"
;--------------------------------
;General

!define LicenseFileLocation "..\LICENSE"

 ;Name and file
  Name "Orko - Trading system"
  OutFile "orko-0.0.1.0-setup.exe"
  LicenseData "..\LICENSE"


  ;Default installation folder
  InstallDir "$LOCALAPPDATA\orko"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\orko" ""

  ;Request application privileges
  RequestExecutionLevel admin
  
  
;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "..\LICENSE"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro CUSTOM_PAGE_JREINFO
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "Check Java installation" JavaInstallation

  SetOutPath "$INSTDIR"
  
  call DownloadAndInstallJREIfNecessary

  ;Store installation folder
  WriteRegStr HKCU "Software\orko" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

SectionEnd

Section "Orko installation" OrkoInstallation

  MessageBox MB_OK "Wrap the JAR as a window service so it runs on startup"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_JavaInstallation ${LANG_ENGLISH} "Java installation section."
  LangString DESC_OrkoInstallation ${LANG_ENGLISH} "Orko installation section."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${JavaInstallation} $(DESC_JavaInstallation)
    !insertmacro MUI_DESCRIPTION_TEXT ${OrkoInstallation} $(DESC_OrkoInstallation)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  Delete "$INSTDIR\Uninstall.exe"

  RMDir "$INSTDIR"

  DeleteRegKey /ifempty HKCU "Software\orko"

SectionEnd
