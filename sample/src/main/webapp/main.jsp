<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <style type="text/css">html, body {
        height: 100%;
        margin: 0;
    }</style>
    <link rel="shortcut icon" type="image/vnd.microsoft.icon" href="/sample/VAADIN/themes/sampleTheme/favicon.ico"/>
    <link rel="icon" type="image/vnd.microsoft.icon" href="/sample/VAADIN/themes/sampleTheme/favicon.ico"/>
    <title>Application</title>
</head>
<body>

<p><a href="app/Accounts" target="sample">Accounts</a></p>

<p><a href="app/Contacts">Contacts</a></p>

<script type="text/javascript">
    if (!vaadin || !vaadin.vaadinConfigurations) {
        if (!vaadin) {
            var vaadin = {}
        }
        vaadin.vaadinConfigurations = {};
        if (!vaadin.themesLoaded) {
            vaadin.themesLoaded = {};
        }
        document.write('<iframe tabIndex="-1" id="__gwt_historyFrame" style="position:absolute;width:0;height:0;border:0;overflow:hidden;" src="javascript:false"></iframe>');
        document.write("<script language='javascript' src='/sample/VAADIN/widgetsets/com.expressui.core.view.AppWidgetSet/com.expressui.core.view.AppWidgetSet.nocache.js?1324605579998'><\/script>");
    }
    vaadin.vaadinConfigurations["sample"] = {
        appUri:'/sample/app',
        standalone:true,
        themeUri:"/sample/VAADIN/themes/sampleTheme",
        versionInfo:{
            vaadinVersion:"6.7.3",
            applicationVersion:"NONVERSIONED"
        },
        "comErrMsg":{
            "caption":"Communication problem",
            "message":"Take note of any unsaved data, and <u>click here<\/u> to continue.",
            "url":"mvc\/login.do"
        },
        "authErrMsg":{
            "caption":"Authentication problem",
            "message":"Take note of any unsaved data, and <u>click here<\/u> to continue.", "url":null
        }
    };
</script>
<script type="text/javascript">
    if (!vaadin.themesLoaded['sampleTheme']) {
        var stylesheet = document.createElement('link');
        stylesheet.setAttribute('rel', 'stylesheet');
        stylesheet.setAttribute('type', 'text/css');
        stylesheet.setAttribute('href', '/sample/VAADIN/themes/sampleTheme/styles.css');
        document.getElementsByTagName('head')[0].appendChild(stylesheet);
        vaadin.themesLoaded['sampleTheme'] = true;
    }
</script>
<script type="text/javascript">
    setTimeout('if (typeof com_expressui_core_view_AppWidgetSet == "undefined") {alert("Failed to load the widgetset: /sample/VAADIN/widgetsets/com.expressui.core.view.AppWidgetSet/com.expressui.core.view.AppWidgetSet.nocache.js?1324605579998")};', 15000);
</script>
<div id="sample" class="v-app v-theme-sampleTheme v-app-MainApplication">
    <div class="v-app-loading"></div>
</div>
<noscript>You have to enable javascript in your browser to use an application built with Vaadin.</noscript>

</body>
</html>