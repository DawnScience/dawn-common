cmake_minimum_required (VERSION 2.8.6)
PROJECT (HDFJAVA_NCSA_HDF_VIEW)

SET (CMAKE_VERBOSE_MAKEFILE 1)

INCLUDE_DIRECTORIES (
    ${JAVA_NCSA_HDF_HDF4_BINARY_DIR}
    ${JAVA_NCSA_HDF_HDF5_BINARY_DIR}
    ${JAVA_NCSA_HDF_OBJECT_BINARY_DIR}
    ${HDFJAVA_LIB_DIR}
)

SET (HDFJAVA_NCSA_HDF_VIEW_SRCS
    Chart.java
    DataOptionDialog.java
    DataView.java
    DefaultFileFilter.java
    DefaultImageView.java
    DefaultMetaDataView.java
    DefaultPaletteView.java
    DefaultTableView.java
    DefaultTextView.java
    DefaultTreeView.java
    FileConversionDialog.java
    HDFView.java
    HelpView.java
    ImageView.java
    MathConversionDialog.java
    MetaDataView.java
    NewAttributeDialog.java
    NewDatasetDialog.java
    NewDatatypeDialog.java
    NewFileDialog.java
    NewGroupDialog.java
    NewImageDialog.java
    NewLinkDialog.java
    NewTableDataDialog.java
    PaletteView.java
    TableView.java
    TextView.java
    Tools.java
    TreeView.java
    UserOptionsDialog.java
    ViewManager.java
    ViewProperties.java
)
SET (HDFJAVA_NCSA_HDF_VIEW_ICONS
    icons/animation.gif
    icons/apps.gif
    icons/audio.gif
    icons/autocontrast.gif
    icons/blank.gif
    icons/brightness.gif
    icons/chart.gif
    icons/copy.gif
    icons/cut.gif
    icons/dataset.gif
    icons/datasetA.gif
    icons/datatype.gif
    icons/datatypeA.gif
    icons/fileclose.gif
    icons/filenew.gif
    icons/fileopen.gif
    icons/filesave.gif
    icons/first.gif
    icons/folderclose.gif
    icons/foldercloseA.gif
    icons/folderopen.gif
    icons/folderopenA.gif
    icons/hdf_large.gif
    icons/hdf.gif
    icons/hdf32.gif
    icons/hdf32.icns
    icons/hdf32.png
    icons/hdf4.gif
    icons/hdf5.gif
    icons/help.gif
    icons/image.gif
    icons/imageA.gif
    icons/last.gif
    icons/link.gif
    icons/next.gif
    icons/palette.gif
    icons/paste.gif
    icons/pdf.gif
    icons/previous.gif
    icons/question.gif
    icons/table.gif
    icons/tableA.gif
    icons/text.gif
    icons/textA.gif
    icons/txt.gif
    icons/url.gif
    icons/video.gif
    icons/xls.gif
    icons/zoomin.gif
    icons/zoomout.gif
)

SET (HDFJAVA_NCSA_HDF_VIEW_HTML
    NewAttrHelp.html
    NewDatsetHelp.html
)

SET (CMAKE_JAVA_INCLUDE_PATH 
    ${CMAKE_JAVA_INCLUDE_PATH} 
    ${HDFJAVA_JARS_TO_EXPORT}
)

INCLUDE_DIRECTORIES (${CMAKE_CURRENT_BINARY_DIR}/CMakeFiles/${HDFJAVA_NCSA_VIEW_LIB_CORENAME}.dir/ncsa/hdf/object)

SET (CMAKE_JAVA_RESOURCE_PATH "ncsa/hdf/view")
ADD_JAR (${HDFJAVA_NCSA_VIEW_LIB_TARGET} ${HDFJAVA_NCSA_HDF_VIEW_SRCS} ${HDFJAVA_NCSA_HDF_VIEW_ICONS})
INSTALL_JAR (${HDFJAVA_NCSA_VIEW_LIB_TARGET} ${HJAVA_INSTALL_LIB_DIR} apps)
GET_TARGET_PROPERTY(${HDFJAVA_NCSA_VIEW_LIB_TARGET}_JAR_FILE ${HDFJAVA_NCSA_VIEW_LIB_TARGET} JAR_FILE)
SET_GLOBAL_VARIABLE (HDFJAVA_HDFVIEWJAR_TO_EXPORT "${HDFJAVA_HDFVIEWJAR_TO_EXPORT};${${HDFJAVA_NCSA_VIEW_LIB_TARGET}_JAR_FILE}")
ADD_DEPENDENCIES (${HDFJAVA_NCSA_VIEW_LIB_TARGET} ${HDFJAVA_NCSA_OBJECT_LIB_TARGET})

IF (UNIX)
  SET (HDFVIEW_SCRIPT "hdfview.sh")
ELSE (UNIX)
  SET (HDFVIEW_SCRIPT "hdfview.bat")
ENDIF (UNIX)

IF (WIN32 AND NOT CYGWIN)
  SET (CMAKE_JAVA_INCLUDE_FLAG_SEP ";")
  SET (CMAKE_JAVA_PATH_EXPORT "set PATH=@CMAKE_INSTALL_PREFIX@/lib;%PATH%")
ELSE (WIN32 AND NOT CYGWIN)
  SET (CMAKE_JAVA_INCLUDE_FLAG_SEP ":")
  SET (CMAKE_JAVA_PATH_EXPORT "export LD_LIBRARY_PATH=@CMAKE_INSTALL_PREFIX@/lib:$LD_LIBRARY_PATH")
ENDIF (WIN32 AND NOT CYGWIN)

SET (HDFVIEW_CLASSPATH "${HDFVIEW_CLASSPATH}@CMAKE_INSTALL_PREFIX@/lib")
FOREACH (HCP_JAR ${HDFJAVA_JARS_TO_EXPORT})
  GET_FILENAME_COMPONENT (_HCP_FILE ${HCP_JAR} NAME)
  SET (HDFVIEW_CLASSPATH "@CMAKE_INSTALL_PREFIX@/lib/${_HCP_FILE}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${HDFVIEW_CLASSPATH}")
ENDFOREACH (HCP_JAR ${HDFJAVA_JARS_TO_EXPORT})
FOREACH (HCP_JAR ${HDFJAVA_HDFVIEWJAR_TO_EXPORT})
  GET_FILENAME_COMPONENT (_HCP_FILE ${HCP_JAR} NAME)
  SET (HDFVIEW_CLASSPATH "@CMAKE_INSTALL_PREFIX@/lib/${_HCP_FILE}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${HDFVIEW_CLASSPATH}")
ENDFOREACH (HCP_JAR ${HDFJAVA_HDFVIEWJAR_TO_EXPORT})

FILE (WRITE ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_SCRIPT}.cmake.in
  "${CMAKE_JAVA_PATH_EXPORT}
  ${CMAKE_Java_RUNTIME} -Xmx1024M -Djava.library.path=\"@CMAKE_INSTALL_PREFIX@/lib\" -Dhdfview.root=\"@CMAKE_INSTALL_PREFIX@/lib\" -cp \"${HDFVIEW_CLASSPATH}\" ncsa.hdf.view.HDFView -root \"@CMAKE_INSTALL_PREFIX@/share\"
  "
)

CONFIGURE_FILE (
    ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_SCRIPT}.cmake.in
    ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_SCRIPT} @ONLY
)

IF (BUILD_TESTING)
  IF (UNIX)
    SET (HDFVIEW_TEST_SCRIPT "thdfview.sh")
  ELSE (UNIX)
    SET (HDFVIEW_TEST_SCRIPT "thdfview.bat")
  ENDIF (UNIX)

  SET (HDFVIEW_TEST_CLASSPATH "${HDFVIEW_CLASSPATH}${CMAKE_SOURCE_DIR}/lib")
  FOREACH (HCP_JAR ${HDFJAVA_JARS_TO_EXPORT})
    SET (HDFVIEW_TEST_CLASSPATH "${HCP_JAR}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${HDFVIEW_TEST_CLASSPATH}")
  ENDFOREACH (HCP_JAR ${HDFJAVA_JARS_TO_EXPORT})
  FOREACH (HCP_JAR ${HDFJAVA_HDFVIEWJAR_TO_EXPORT})
    SET (HDFVIEW_TEST_CLASSPATH "${HCP_JAR}${CMAKE_JAVA_INCLUDE_FLAG_SEP}${HDFVIEW_TEST_CLASSPATH}")
  ENDFOREACH (HCP_JAR ${HDFJAVA_HDFVIEWJAR_TO_EXPORT})

  FILE (WRITE ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_TEST_SCRIPT}.cmake.in
    "${CMAKE_JAVA_PATH_EXPORT}
  ${CMAKE_Java_RUNTIME} -Xmx1024M -Djava.library.path=${CMAKE_RUNTIME_OUTPUT_DIRECTORY} -Dhdfview.root=${PROJECT_BINARY_DIR} -cp \"${HDFVIEW_TEST_CLASSPATH}\" ncsa.hdf.view.HDFView -root ${PROJECT_BINARY_DIR}
    "
  )

  CONFIGURE_FILE (
      ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_TEST_SCRIPT}.cmake.in
      ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_TEST_SCRIPT} @ONLY
  )
ENDIF (BUILD_TESTING)
  
INSTALL (
    FILES ${HDFJAVA_NCSA_HDF_VIEW_BINARY_DIR}/${HDFVIEW_SCRIPT}
    DESTINATION ${HJAVA_INSTALL_BIN_DIR}
    COMPONENT apps
    PERMISSIONS OWNER_WRITE OWNER_READ OWNER_EXECUTE GROUP_READ GROUP_EXECUTE WORLD_EXECUTE
)
