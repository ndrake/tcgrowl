<%@ include file="/include.jsp" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<script type="text/javascript">
    function sendTest() {
    
        var gs = $('properties[tcgrowl.gServer].value');
        if(!gs || gs.value.length == 0) {
            alert("Please enter the Growl server's IP address.");
            return;
        }
        var gp = $('properties[tcgrowl.gPassword].value');
        
        var gm = $('growlTestMessage').value;
        if(!gm || gm.length ==0) {
            return;
        }
    
        BS.ajaxRequest($('growlTestForm').action, {
            parameters: 'growlServer='+ gs.value + (gp&&gp.value.length>0?'&growlPassword='+gp.value+'&':'&')+'growlTestMessage='+gm,
            onComplete: function(transport) {
              if (transport.responseXML) {
                  $('tcGrowlTest').refresh();
              }             
            }
        });
        return false;        
    }
</script>

<bs:refreshable containerId="tcGrowlTest" pageUrl="${pageUrl}">
<bs:messages key="tcgrowlMessage"/>

<form action="/tcgrowlSettings.html" method="post" id="growlTestForm">
Send test message to Growl server: <input id="growlTestMessage" name="growlTestMessage" type="text" />  <input type="button" name="Test" value="Test" onclick="return sendTest();"/>
</form>
</bs:refreshable>