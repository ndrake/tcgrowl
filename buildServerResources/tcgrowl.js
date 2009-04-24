var TCGrowl = {
  runAction: function() {
    BS.ajaxRequest($('growlTestForm').action, {
      //parameters: 'growlTestMessage=' + curAction + '&j2eeProfilingEnabled=' + $('j2eeProfilingEnabled').checked,
      onComplete: function(transport) {
        if (transport.responseXML) {
          BS.XMLResponse.processErrors(transport.responseXML, {
            onProfilerProblemError: function(elem) {
              alert(elem.firstChild.nodeValue);
            }
          });
        }
        alert("BlAH!");
      }
    });
    return false;
  }
};
