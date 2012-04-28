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
    success: function(xml) { setupEditor(xml); }
  });


// set up button callbacks
$('#titleAdd').click(function() { addTitleInfo(); });

$('#sendXML').click(function() { sendXML(); });


}); // document ready


// Method to create elements
function createElement(element, parentElement, count, containerId, indent) {
	var existingElement = false;
	var cleanContainerId = containerId.substring(1);	
	var elementContainerId = cleanContainerId+'_'+element.getTitle()+'Instance'+count;

	$('<div/>').attr({'id' : elementContainerId, 'class' : element.getTitle()+'Instance'}).appendTo(containerId);

	// See if element already exists.  If not, create it and add it to xml document
	var numElements = $(parentElement).children(element.getTitle()).length
	if( numElements >= count ) {
		existingElement = true;
	} else {
		$('<'+element.getTitle()+'/>').appendTo(parentElement);
	}

	// set up element title and entry field if appropriate
	if(element.getType() == 'none') {
		createElementText(element.getTitle(), '#'+elementContainerId);
	} else if(element.getType() == 'string') {
		var valueValue = '';
		if(existingElement) {
			valueValue = $(parentElement).children(element.getTitle()).eq(count).text();
		}
		alert(elementContainerId+'_'+element.getTitle()+" "+count);
		createElementLabelAndInput(containerId+'_'+element.getTitle(), element.getType(), element.getTitle(), valueValue, '#'+elementContainerId, count, parentElement);
	}

	// add elements
	var elementsArray = element.getElements();
	for (var i = 0; i < elementsArray.length; i++) {
		var elementCount = $(parentElement).children(element.getTitle()).eq(count).children(elementsArray[i].getTitle()).length;
	
		for(var j = 0; j < elementCount; j++) {
			
			createElement(elementsArray[i], $(parentElement).children(element.getTitle()).eq(count), j, '#'+elementContainerId, 4);
		}
	}
}

function createElementText(nameValue, appendValue) {
		$('<h3/>').text(nameValue).appendTo(appendValue);
}

function createElementLabel(forValue, nameValue, appendValue) {
		$('<label/>').attr({'for' : forValue }).text(nameValue).appendTo(appendValue);
}

function createElementInput(idValue, typeValue, nameValue, valueValue, appendValue) {
	$('<input/>').attr({'id' : idValue, 'type' : typeValue, 'name' : nameValue, 'value' : valueValue}).appendTo(appendValue);
}

function createElementLabelAndInput(idValue, typeValue, nameValue, valueValue, appendValue, countValue, parentValue) {

	
	createElementLabel(idValue+countValue, nameValue, appendValue);
	createElementInput(idValue+countValue, typeValue, nameValue, valueValue, appendValue);

        // Change	
	$('#'+idValue+countValue).on('change', { value : countValue }, function(event) {
		
		var numElements = $(parentValue).children(nameValue).eq(countValue).children(idValue).length
		$(parentValue).children(nameValue).eq(countValue).text($('#'+idValue+event.data.value).val());
	});
}

function addTitleInfoElements() {

	var num     = $('#titleInfo > .titleInfoInstance').length; 
	
	if(num == undefined) num = 0;

	

	createElement(TitleInfo, $(window.MyVariables.xml).find("mods"), num, '#titleInfo', 2);

	$('<br/>').appendTo('#titleInfoInstance'+num);

//	$('<input>').attr({'type' : 'button', 'value' : 'X', 'id' : 'titleDel'+newNum}).appendTo('#titleDiv'+newNum);
//	$('<br/>').appendTo('#titleDiv'+newNum);

//	$('#titleDel'+newNum).on('click', { value : newNum -1 }, function(event) {
//		alert('titleDel'+event.data.value+' called!');
//		// delete selected titleInfo from XML
//		$(window.MyVariables.xml).find("mods").children('titleInfo').eq(event.data.value).remove();

//		// redisplay titleInfo listing
//		$('#titleInfo').children().remove();
//		$(window.MyVariables.xml).find('mods').children("titleInfo").each(function() { addTitleInfo(); });
//	 });

}




function createLabelAndInput(textValue, idValue, typeValue, nameValue, valueValue, appendValue, changeValue, objectValue) {
	createLabel(idValue+changeValue, textValue, appendValue);
	createInput(idValue+changeValue, typeValue, nameValue, valueValue, appendValue);

        // Change	
	$('#'+idValue+changeValue).on('change', { value : changeValue }, function(event) {
		alert(idValue+changeValue+' change called');
		var numElements = $(window.MyVariables.xml).find("mods").children(objectValue).eq(changeValue - 1).children(idValue).length
		if( numElements == 0 ){
			$('<'+idValue+'/>').appendTo($(window.MyVariables.xml).find("mods").children(objectValue).eq(changeValue - 1));
		}
		$(window.MyVariables.xml).find("mods").children(objectValue).eq(changeValue - 1).children(idValue).eq(0).text($('#'+idValue+event.data.value).val());
	});
}

function createLabel(forValue, textValue, appendValue) {
	$('<label/>').attr({'for' : forValue }).text(textValue).appendTo(appendValue);
}

function createInput(idValue, typeValue, nameValue, valueValue, appendValue) {
	$('<input/>').attr({'id' : idValue, 'type' : typeValue, 'name' : nameValue, 'value' : valueValue}).appendTo(appendValue);
}

function addTitleInfo() {

	var num     = $('.titleInput').length; 
	
	if(num == undefined) num = 0;

	var newNum  = new Number(num + 1);      // the numeric ID of the new input field being added

	var newElem = $('<div/>').attr({'id' : 'titleDiv'+newNum, 'class' : 'titleInput'}).appendTo('#titleInfo');
	createLabelAndInput('Title', 'title', 'text', 'title'+newNum, $(window.MyVariables.xml).find("mods").children("titleInfo").eq(newNum - 1).children("title").eq(0).text(), '#titleDiv'+newNum, newNum, 'titleInfo');
	$('<br/>').appendTo('#titleDiv'+newNum);
	createLabelAndInput('Subtitle', 'subTitle', 'text', 'subTitle'+newNum, $(window.MyVariables.xml).find("mods").children("titleInfo").eq(newNum - 1).children("subTitle").eq(0).text(), '#titleDiv'+newNum, newNum, 'titleInfo');
	$('<br/>').appendTo('#titleDiv'+newNum);
//	createLabelAndInput('Part Number', 'partNumber'+newNum, 'text', 'partNumber'+newNum, $(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("partNumber").eq(0).text(), '#titleDiv'+newNum, newNum, $(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("partNumber").eq(0));
	$('<br/>').appendTo('#titleDiv'+newNum);
//	createLabelAndInput('Part Name', 'partName'+newNum, 'text', 'partName'+newNum, $(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("partName").eq(0).text(), '#titleDiv'+newNum, newNum, $(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("partName").eq(0));
	$('<br/>').appendTo('#titleDiv'+newNum);
//	createLabelAndInput('Non Sort', 'nonSort'+newNum, 'text', 'nonSort'+newNum, $(window.MyVariables.xml).find("titleInfo").eq(newNum - 1).find("nonSort").eq(0).text(), '#titleDiv'+newNum, newNum, $(window.MyVariables.xml).find("nonSort").eq(newNum - 1).find("title").eq(0));

	$('<input>').attr({'type' : 'button', 'value' : 'X', 'id' : 'titleDel'+newNum}).appendTo('#titleDiv'+newNum);
	$('<br/>').appendTo('#titleDiv'+newNum);

	$('#titleDel'+newNum).on('click', { value : newNum -1 }, function(event) {
		alert('titleDel'+event.data.value+' called!');
		// delete selected titleInfo from XML
		$(window.MyVariables.xml).find("mods").children('titleInfo').eq(event.data.value).remove();

		// redisplay titleInfo listing
		$('#titleInfo').children().remove();
		$(window.MyVariables.xml).find('mods').children("titleInfo").each(function() { addTitleInfo(); });
	 });

}

function deleteTitleInfo() {
       var num = $('.titleInput').length;
	if(num == undefined) num = 0;

        if(num > 0) $('#titleDiv' + num).remove();     // remove the last element
 
        // if only one element remains, disable the "remove" button
        if (num == 1)
               $('#titleDel').attr('disabled','disabled');
}


function setupEditor(xml)
{
  // make XML accessible to rest of code
  window.MyVariables.xml = xml;

//  $('<'+'fakeElement'+'/>').appendTo($(window.MyVariables.xml).find("mods").children("titleInfo").eq(0));
//  $(window.MyVariables.xml).find("mods").children("titleInfo").eq(0).children("fakeElement").attr({"test" : "one"});
//  $(window.MyVariables.xml).find("mods").children("titleInfo").eq(0).children("fakeElement").removeAttr("test");

  // preload the title
  $(xml).find('mods').children("titleInfo").each(function() { addTitleInfoElements(); });

}

// Send XML back to be stored
function sendXML() {
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
}


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

var ID_attr = {
	title : 'ID',
	type : 'string',
	defaultValue : null,
	values : [],
	"getTitle" : function() {
		return this.title;
	},
	"getType" : function() {
		return this.type;
	},
	"getDefault" : function() {
		return this.defaultValue;
	},
	"getValues" : function() {
		return this.values;
	}
}

var type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['abbreviated', 'translated', 'alternative', 'uniform'],
	"getTitle" : function() {
		return this.title;
	},
	"getType" : function() {
		return this.type;
	},
	"getDefault" : function() {
		return this.defaultValue;
	},
	"getValues" : function() {
		return this.values;
	}
}

var Title = {
	title : 'title',
	repeatable : true,
	type : 'string',
	singleton : false,
        attributes : [ ],
	elements : [ ],
 
	"getTitle" : function() {
		return this.title;
	},
	"isRepeatable" : function() {
		return this.repeatable;
	},
	"getType" : function() {
		return this.type;
	},
	"isSingleton" : function() {
		return this.singleton;
	},
	"getAttributes" : function() {
		return this.attributes;
	},
	"getElements" : function() {
		return this.elements;
	}
};


var TitleInfo = {
	title : 'titleInfo',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ID_attr, type_attr ],
	elements : [ Title ],
 
	"getTitle" : function() {
		return this.title;
	},
	"isRepeatable" : function() {
		return this.repeatable;
	},
	"getType" : function() {
		return this.type;
	},
	"isSingleton" : function() {
		return this.singleton;
	},
	"getAttributes" : function() {
		return this.attributes;
	},
	"getElements" : function() {
		return this.elements;
	}
};


</script>
</body>
