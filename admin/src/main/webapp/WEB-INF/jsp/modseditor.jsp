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

<form id="myForm">
<div id="titleInfo">
</div>
<br/>
    <div>
        <input type="button" id="titleInfoAdd" value="Add titleInfo" />
    </div>
<br/><br/>
<div id="name"/>
</div>
<br/>
    <div>
        <input type="button" id="nameAdd" value="Add name" />
    </div>
<br/><br/>
<div id="typeOfResource"></div>
<br/>
    <div>
        <input type="button" id="typeOfResourceAdd" value="Add typeOfResource" />
    </div>
<br/><br/>
<div id="genre"></div>
<br/>
    <div>
        <input type="button" id="genreAdd" value="Add genre" />
    </div>
<br/><br/>
<div id="originInfo"></div>
<br/>
    <div>
        <input type="button" id="originInfoAdd" value="Add originInfo" />
    </div>
<br/><br/>
<div id="language"></div>
<div id="physicalDescription"></div>
<div id="abstract"></div>
<div id="tableOfContents"></div>
<div id="targetAudience"></div>
<div id="note"></div>
<div id="subject"></div>
<div id="classification"></div>
<div id="relatedItem"></div>
<div id="identifier"></div>
<div id="location"></div>
<div id="accessCondition"></div>
<div id="part"></div>
<div id="extension"></div>
<div id="recordInfo"></div>

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
$('#titleInfoAdd').click(function() { addTitleInfoElements(); });
$('#nameAdd').click(function() { addNameElements(); });
$('#typeOfResourceAdd').click(function() { addTypeOfResourceElements(); });
$('#genreAdd').click(function() { addGenreElements(); });
$('#originInfoAdd').click(function() { addOriginInfoElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });
$('#Add').click(function() { addElements(); });

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
	//alert("numElements: "+numElements+' count: '+count);
	if( numElements > count ) {
		existingElement = true;
	} else {
		//alert("creating element in XML: "+element.getTitle());
		$('<'+element.getTitle()+'/>').appendTo(parentElement);
	}

	// set up element title and entry field if appropriate
	if(element.getType() == 'none') {
		createElementText(element.getTitle(), '#'+elementContainerId);
	} else {
		var valueValue = '';
		if(existingElement) {
			valueValue = $(parentElement).children(element.getTitle()).eq(count).text();
		}
		createElementLabelAndInput(element, cleanContainerId+'_'+element.getTitle(), valueValue, '#'+elementContainerId, count, parentElement);
	}

	$('<input>').attr({'type' : 'button', 'value' : 'X', 'id' : cleanContainerId+'_'+element.getTitle()+'Del'+count}).appendTo('#'+elementContainerId);
	
	$('#'+cleanContainerId+'_'+element.getTitle()+'Del'+count).on('click', { value : count }, function(event) {
		
		// delete selected titleInfo from XML
		$(parentElement).children(element.getTitle()).eq(event.data.value).remove();

		// redisplay titleInfo listing
		$(containerId).children("."+element.getTitle()+'Instance').remove();
		$(parentElement).children(element.getTitle()).each(function() { 
			var num = $(containerId > '.'+element.getTitle()+'Instance').length; 
	
			if(num == undefined) num = 0;

			createElement(element, parentElement, num, containerId, indent);
		});
	 });



	// add attributes
	var attributesArray = element.getAttributes();
	var hasAttributes = (attributesArray.length > 0 ? true : false);

	if(hasAttributes) {
		// add attribute div show/hide button
		$('<input>').attr({'type' : 'button', 'value' : 'Attributes', 'id' : elementContainerId+'_attrs'}).appendTo('#'+elementContainerId);
		$('#'+elementContainerId+'_attrs').on('click', function() { 
			$('#'+elementContainerId+'_attrsDiv').toggle();
		});	
	}

	// add element buttons
	var elementsArray = element.getElements();
	for (var i = 0; i < elementsArray.length; i++) {
		addElementButton(element, elementContainerId, parentElement, elementsArray[i], count, indent);
	}

	// attribute div	
	if(hasAttributes) {
		// add attribute div hidden
		$('<br/>').appendTo('#'+elementContainerId);
		$('<div/>').attr({'id' : elementContainerId+'_attrsDiv'}).appendTo('#'+elementContainerId).hide();

		// populate attribute div with attribute entry fields
		for (var i = 0; i < attributesArray.length; i++) {				
			createAttribute(elementContainerId+"_"+attributesArray[i].getTitle(), attributesArray[i], parentElement, element.getTitle(), count, '#'+elementContainerId+'_attrsDiv', 2);
			$('<br/>').appendTo('#'+elementContainerId+'_attrsDiv');
		}
	}

	// add elements
	var elementsArray = element.getElements();
	for (var i = 0; i < elementsArray.length; i++) {
		var elementCount = $(parentElement).children(element.getTitle()).eq(count).children(elementsArray[i].getTitle()).length;
	
		for(var j = 0; j < elementCount; j++) {
			createElement(elementsArray[i], $(parentElement).children(element.getTitle()).eq(count), j, '#'+elementContainerId, 4);
		}
	}

	
	$('<br/><br/>').appendTo('#'+elementContainerId);
}


function addElementButton(element, elementContainerId, parentElement, childElement, count, indent) {

		$('<input>').attr({'type' : 'button', 'value' : 'Add '+childElement.getTitle(), 'id' : elementContainerId+'_'+childElement.getTitle()+'_Add'}).appendTo('#'+elementContainerId);

		$('#'+elementContainerId+'_'+childElement.getTitle()+'_Add').on('click', addElementButtonCallback(element, elementContainerId, parentElement, childElement, count, indent));
}

function addElementButtonCallback(element, elementContainerId, parentElement, childElement, count, indent) {
	return function() {	
		var elementsArray = element.getElements();
		var num = $('#'+elementContainerId).children("."+childElement.getTitle()+'Instance').length; 

		if(num == undefined) num = 0; // if no elements, start with zero

		createElement(childElement, $(parentElement).children(element.getTitle()).eq(count), num, '#'+elementContainerId, indent);
	}
}


function createAttribute(idValue, attributeValue, parentValue, nameValue, countValue, appendValue, indentValue) {
	$('<label/>').attr({'for' : idValue }).text(attributeValue.getTitle()).appendTo(appendValue);

	var value = $(parentValue).children(nameValue).eq(countValue).attr(attributeValue.getTitle());

	if(value) {
		; // Should I do any cleanup/formatting?
		
	} else if(attributeValue.getDefault()) {
		value = attributeValue.getDefault();
	} else value = '';

	if(attributeValue.getType() == 'text') {
		$('<input/>').attr({'id' : idValue, 'type' : 'text', 'name' : attributeValue.getTitle(), 'value' : value}).appendTo(appendValue);
	} else if(attributeValue.getType() == 'selection') {

		var selectionValues = attributeValue.getValues();

		var s = $('<select />').attr({'id' : idValue, 'name' : attributeValue.getTitle(), 'value' : value}).appendTo(appendValue);

		for(var val in selectionValues) {

			if(value == selectionValues[val]) {				
	    			$('<option />', {value: selectionValues[val], text: selectionValues[val], selected : true}).appendTo(s);
			} else {
    				$('<option />', {value: selectionValues[val], text: selectionValues[val]}).appendTo(s);
			}
		}
	}

       // Change	
	$('#'+idValue).on('change', function(event) {	
		if($('#'+idValue).val()) {
			$(parentValue).children(nameValue).eq(countValue).attr(attributeValue.getTitle(), $('#'+idValue).val());
		} else {
			// remove empty attribute
			$(parentValue).children(nameValue).eq(countValue).removeAttr(attributeValue.getTitle());
		}
	});
}

function createElementText(nameValue, appendValue) {
		$('<p/>').text(nameValue+' ').appendTo(appendValue);
}

function createElementLabel(forValue, nameValue, appendValue) {
		$('<label/>').attr({'for' : forValue }).text(nameValue+' ').appendTo(appendValue);
}

function createElementInput(element, idValue, valueValue, appendValue) {
	if(element.getType() == 'text') {
		$('<input/>').attr({'id' : idValue, 'type' : element.getType(), 'name' : element.getTitle(), 'value' : valueValue}).appendTo(appendValue);
	} else if(element.getType() == 'selection') {

		var selectionValues = element.getValues();

		var s = $('<select />').attr({'id' : idValue, 'name' : element.getTitle(), 'value' : valueValue}).appendTo(appendValue);

		for(var val in selectionValues) {

			if(valueValue == selectionValues[val]) {				
	    			$('<option />', {value: selectionValues[val], text: selectionValues[val], selected : true}).appendTo(s);
			} else {
    				$('<option />', {value: selectionValues[val], text: selectionValues[val]}).appendTo(s);
			}
		}
	}

}

function createElementChangeCallback(element, parentValue, countValue) {
	return function() {
		$(parentValue).children(nameValue).eq(countValue).text($('#'+idValue+event.data.value).val());
	}
}

function createElementLabelAndInput(element, idValue, valueValue, appendValue, countValue, parentValue) {
	createElementLabel(idValue+countValue, element.getTitle(), appendValue);
	createElementInput(element, idValue+countValue, valueValue, appendValue);

        // Change	
	$('#'+idValue+countValue).on('change', createElementChangeCallback(element, parentValue, countValue));	
}

function addTitleInfoElements() {

	var num = $('#titleInfo > .titleInfoInstance').length; 
	
	if(num == undefined) num = 0;

	createElement(TitleInfo, $(window.MyVariables.xml).find("mods"), num, '#titleInfo', 2);
}
function addNameElements() {

	var num = $('#name > .nameInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Name, $(window.MyVariables.xml).find("mods"), num, '#name', 2);
}

function addTypeOfResourceElements() {

	var num = $('#name > .typeOfResourceInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(TypeOfResource, $(window.MyVariables.xml).find("mods"), num, '#typeOfResource', 2);
}

function addGenreElements() {

	var num = $('#name > .genreInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(Genre, $(window.MyVariables.xml).find("mods"), num, '#genre', 2);
}

function addOriginInfoElements() {

	var num = $('#name > .originInfoInstance').length; 
	
	if(num == undefined) num = 0;
	
	createElement(OriginInfo, $(window.MyVariables.xml).find("mods"), num, '#originInfo', 2);
}




function setupEditor(xml)
{
  // make XML accessible to rest of code
  window.MyVariables.xml = xml;

//  $('<'+'fakeElement'+'/>').appendTo($(window.MyVariables.xml).find("mods").children("titleInfo").eq(0));
//  $(window.MyVariables.xml).find("mods").children("titleInfo").eq(0).children("fakeElement").attr({"test" : "one"});
//  $(window.MyVariables.xml).find("mods").children("titleInfo").eq(0).children("fakeElement").removeAttr("test");

  // preload the title
  $(window.MyVariables.xml).find('mods').children("titleInfo").each(function() { addTitleInfoElements(); });
  $(window.MyVariables.xml).find('mods').children("name").each(function() { addNameElements(); });
  $(window.MyVariables.xml).find('mods').children("typeOfResource").each(function() { addTypeOfResourceElements(); });
  $(window.MyVariables.xml).find('mods').children("genre").each(function() { addGenreElements(); });
  $(window.MyVariables.xml).find('mods').children("originInfo").each(function() { addOriginInfoElements(); });
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

placeTerm_type_attr, placeTerm_authority_attr

var collection_attr = {
	title : 'collection',
	type : 'text',
	defaultValue : 'yes',
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

var manuscript_attr = {
	title : 'manuscript',
	type : 'text',
	defaultValue : 'yes',
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

var nameTitleGroup_attr = {
	title : 'nameTitleGroup',
	type : 'text',
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

var altRepGroup_attr = {
	title : 'altRepGroup',
	type : 'text',
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

var usage_attr = {
	title : 'usage',
	type : 'text',
	defaultValue : 'primary',
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

var supplied_attr = {
	title : 'supplied',
	type : 'text',
	defaultValue : 'yes',
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

var displayLabel_attr = {
	title : 'displayLabel',
	type : 'text',
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

var valueURI_attr = {
	title : 'valueURI',
	type : 'text',
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

var authorityURI_attr = {
	title : 'authorityURI',
	type : 'text',
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

var genre_authority_attr = {
	title : 'authority',
	type : 'text',
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


var authority_attr = {
	title : 'authority',
	type : 'text',
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

var transliteration_attr = {
	title : 'transliteration',
	type : 'text',
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

var script_attr = {
	title : 'script',
	type : 'text',
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

var xmllang_attr = {
	title : 'xml:lang',
	type : 'text',
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

var lang_attr = {
	title : 'lang',
	type : 'text',
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

var xlink_attr = {
	title : 'xlink',
	type : 'text',
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

var ID_attr = {
	title : 'ID',
	type : 'text',
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

var placeTerm_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','code', 'text'],
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

var placeTerm_authority_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','marcgac', 'marcountry', 'iso3166'],
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
var titleInfo_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','abbreviated', 'translated', 'alternative', 'uniform'],
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

var name_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','personal', 'corporate', 'conference', 'family'],
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

var namePart_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','date', 'family', 'given', 'termsOfAddress'],
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

var roleTerm_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','code', 'text'],
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

var genre_type_attr = {
	title : 'type',
	type : 'selection',
	defaultValue : null,
	values : ['','class', 'work type', 'style'],
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
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var SubTitle = {
	title : 'subTitle',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var PartNumber = {
	title : 'partNumber',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var PartName = {
	title : 'partName',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var NonSort = {
	title : 'nonSort',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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
        attributes : [ ID_attr, xlink_attr, xmllang_attr, script_attr, transliteration_attr, titleInfo_type_attr, authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, supplied_attr, usage_attr, altRepGroup_attr, nameTitleGroup_attr ],
	elements : [ Title, SubTitle, PartNumber, PartName, NonSort ],
 
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

var NamePart = {
	title : 'namePart',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ namePart_type_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var DisplayForm = {
	title : 'displayForm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var Affiliation = {
	title : 'affiliation',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var RoleTerm = {
	title : 'roleTerm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var Role = {
	title : 'role',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ ],
	elements : [ RoleTerm ],
 
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

var Description = {
	title : 'description',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var Name = {
	title : 'name',
	repeatable : true,
	type : 'none',
	singleton : false,
        attributes : [ ID_attr, xlink_attr, xmllang_attr, script_attr, transliteration_attr, name_type_attr, authority_attr, authorityURI_attr, valueURI_attr, displayLabel_attr, usage_attr, altRepGroup_attr, nameTitleGroup_attr ],
	elements : [ NamePart, DisplayForm, Affiliation, Role, Description ],
 
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

var TypeOfResource = {
	title : 'typeOfResource',
	repeatable : true,
	type : 'selection',
	singleton : false,
	values : ['','text', 'cartographic', 'notated music', 'sound recording-musical', 'sound recording-nonmusical', 'sound recording', 'still image', 'moving image', 'three dimensional object', 'software', 'multimedia mixed material'],
        attributes : [ collection_attr, manuscript_attr, displayLabel_attr, usage_attr, altRepGroup_attr ],
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
	"getValues" : function() {
		return this.values;
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


var Genre = {
	title : 'genre',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, genre_authority_attr, authorityURI_attr, valueURI_attr, genre_type_attr, displayLabel_attr, usage_attr, altRepGroup_attr],
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

var PlaceTerm = {
	title : 'placeTerm',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ placeTerm_type_attr, placeTerm_authority_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var Place = {
	title : 'place',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ supplied_attr ],
	elements : [ PlaceTerm ],
 
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

var Publisher = {
	title : 'publisher',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ supplied_attr, lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

/*
var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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

var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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


var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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
*/


var OriginInfo = {
	title : 'originInfo',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr, displayLabel_attr, altRepGroup_attr ],
	elements : [ Place, Publisher, DateIssued, DateCreated, DateCaptured, DateValid, DateModified, CopyrightDate, DateOther, Edition, Issuance, Frequency ],
 
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

/*
var  = {
	title : '',
	repeatable : true,
	type : 'text',
	singleton : false,
        attributes : [ lang_attr, xmllang_attr, script_attr, transliteration_attr ],
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
*/
</script>
</body>
