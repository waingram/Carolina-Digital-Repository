<%--

    Copyright 2008 The University of North Carolina at Chapel Hill

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

            http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="header.jsp"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="icon" href="<c:url value='/favicon.ico'/>" type="image/x-icon" />
<title><fmt:message key="updateobject.heading"/></title>
<LINK REL=StyleSheet HREF="<c:url value='/css/unc_styles.css'/>"
	TYPE="text/css" />
<LINK REL=StyleSheet HREF="<c:url value='/css/ir_style.css'/>"
	TYPE="text/css" />

  <script src="http://code.jquery.com/jquery-latest.js"></script>

</head>
<body>
<p id="xmlElement"></p>

<p id="someElement"></p>
<p id="anotherElement"></p>

<form id="myForm">
<div id="titleInfo">

</div>
    <div>
        <input type="button" id="titleAdd" value="add another title" />
        <input type="button" id="titleDel" value="remove last title" />
    </div>
<div id="name"/>
<div id="typeOfResource"/>
<div id="genre"/>
<div id="originInfo"/>
<div id="language"/>
<div id="physicalDescription"/>
<div id="abstract"/>
<div id="tableOfContents"/>
<div id="targetAudience"/>
<div id="note"/>
<div id="subject"/>
<div id="classification"/>
<div id="relatedItem"/>
<div id="identifier"/>
<div id="location"/>
<div id="accessCondition"/>
<div id="part"/>
<div id="extension"/>
<div id="recordInfo"/>

    <div id="input1" style="margin-bottom:4px;" class="clonedInput">
        Name: <input type="text" name="name1" id="name1" />
    </div>
 
    <div>
        <input type="button" id="btnAdd" value="add another name" />
        <input type="button" id="btnDel" value="remove name" />
    </div>

<input type="button" id="sendXML" value="Submit Changes" />
</form>

<script>

$(document).ready(function()
{

window.MyVariables = {};
window.MyVariables.xml = {};

  $.ajax({
    type: "GET",
    url: "https://nagina/cdradmin/modsexample.xml",
    dataType: "xml",
    success: function(xml) { parseInputXml(xml); }
  });


// Set up form
 $('#titleDel').attr('disabled','disabled');

// add new title
$('#titleAdd').click(function() {
	var num     = $('.titleInput').length; 
	
	if(num == undefined) num = 0;

	var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added

	var newElem = $('<div/>').attr({'id' : 'titleDiv'+newNum, 'class' : 'titleInput'}).appendTo('#titleInfo');
	$('<input/>').attr({'id' : 'title'+newNum, 'type' : 'text', 'name' : 'title'+newNum, 'value' : $(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("title").eq(0).text()}).appendTo('#titleDiv'+newNum);


        // Change	
	$('#title'+newNum).on('change', { value : newNum }, function(event) {
		$(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("title").eq(0).text($('#title'+event.data.value).val());
	});

        // enable the "remove" button
        $('#titleDel').removeAttr('disabled');
});

$('#titleDel').click(function() {
                var num = $('.titleInput').length;
	
		if(num == undefined) num = 0;

                if(num > 0) $('#titleDiv' + num).remove();     // remove the last element
 
                // if only one element remains, disable the "remove" button
                if (num == 1)
                    $('#titleDel').attr('disabled','disabled');
});



$('#titleAddOld').click(function() {
                var num     = $('.titleInput').length; // how many "duplicatable" input fields we currently have
                var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added
 
                // create the new element via clone(), and manipulate it's ID using newNum value
                var newElem = $('#input' + num).clone().attr('id', 'input' + newNum);
 
                // manipulate the name/id values of the input inside the new element
                newElem.children(':first').attr('id', 'name' + newNum).attr('name', 'name' + newNum);
 
                // insert the new element after the last "duplicatable" input field
                $('#input' + num).after(newElem);
 
                // enable the "remove" button
                $('#btnDel').removeAttr('disabled');

 
                // business rule: you can only add 5 names
                if (newNum == 5)
                    $('#btnAdd').attr('disabled','disabled');
            });
 



$('#btnAdd').click(function() {
                var num     = $('.clonedInput').length; // how many "duplicatable" input fields we currently have
                var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added
 
                // create the new element via clone(), and manipulate it's ID using newNum value
                var newElem = $('#input' + num).clone().attr('id', 'input' + newNum);
 
                // manipulate the name/id values of the input inside the new element
                newElem.children(':first').attr('id', 'name' + newNum).attr('name', 'name' + newNum);
 
                // insert the new element after the last "duplicatable" input field
                $('#input' + num).after(newElem);
 
                // enable the "remove" button
                $('#btnDel').removeAttr('disabled');

 
                // business rule: you can only add 5 names
                if (newNum == 5)
                    $('#btnAdd').attr('disabled','disabled');
            });
 
            $('#btnDel').click(function() {
                var num = $('.clonedInput').length; // how many "duplicatable" input fields we currently have
                $('#input' + num).remove();     // remove the last element
 
                // enable the "add" button
                $('#btnDel').removeAttr('disabled');
 
                // if only one element remains, disable the "remove" button
                if (num-1 == 1)
                    $('#btnDel').attr('disabled','disabled');

                if (num < 6)
		    $('#btnAdd').removeAttr('disabled');

            });
 

});

function parseInputXml(xml)
{

  window.MyVariables.xml = xml;

  //find every Tutorial and print the author
  $(xml).find("titleInfo").each(function()
  {
    $("#someElement").append($(this).attr("type") + "<br />");
  });

//  sendXml(xml);

}

$('#sendXML').click(function() {




if( !window.XMLSerializer ){
   window.XMLSerializer = function(){};

   window.XMLSerializer.prototype.serializeToString = function( XMLObject ){
      return XMLObject.xml || '';
   };
}

// convert XML DOM to string
var xmlString = xml2Str(window.MyVariables.xml);




        var str = '<?xml version="1.0" encoding="UTF-8"?><foo><bar>Hello World</bar></foo>';
        // var xmlData = strToXml(str); // no need for this unless you want to use it
                                        // on client side
        // console.log($.isXMLDoc(xmlData)); 
        $.ajax({
           url: 'https://nagina/cdradmin/ir/admin/modsform',
           contentType: "application/xml",
           type: "POST",  // type should be POST
           data: xmlString, // send the string directly
           success: function(response){
             alert(response);
           },
           error: function(response) {
              alert(response);
           }
        });
});


function xml2Str(xmlNode)
{
  try {
    // Gecko-based browsers, Safari, Opera.
    return (new XMLSerializer()).serializeToString(xmlNode);
  }
  catch (e) {
    try {
      // Internet Explorer.
      return xmlNode.xml;
    }
    catch (e)
    {//Strange Browser ??
     alert('Xmlserializer not supported');
    }
  }
  return false;
}


</script>


</body>
