<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html ng-app="filesutraApp">
<head>
  <title>File Ops</title>
  <link rel="shortcut icon" href="/images/favicon.ico" />
  <asset:stylesheet href="application.css"/>

  <style>
  .container {
    width:100%;
    overflow-x:hidden;
    height:329px;
  }
    li a,a:focus, a:hover  {
      cursor: pointer;
    }
    .filesPane {
/*      min-height: 400px;
      max-height: 400px;
      overflow: auto;
*/    }
    .selectedItem {
      background-color: #46b8da;
    }
    .filesutraItem {
      cursor: pointer;
    margin:0px;
    }
    .filesutraItem a{
        height:50px;
        margin:3px;
        text-overflow: ellipsis;
        display: inline-block;
        /* width: 150px; */
        white-space: nowrap;
        overflow: hidden;
    }
    .action-group{
      width: 100%;
      position: absolute;
      bottom: 90px;
      margin-left: 30px;
    }
    .list-group-item1{
      position: relative;
      height: 150px;
      width: 150px;
      margin: 0.5cm;
      display: block;
      padding: 10px 15px;
      text-align: center;
      /*border: 1px solid #ddd;*/
    }
    .bottom2 { 
      width: 250px;   
    }
    .import-btn {
      margin-right: 20px;
    }
    .busy, .busy * {  
    cursor: wait !important;  
    }
    .spinner {
    opacity: 0;
    display:none;
    width: 20px;
    height:10px;
        -webkit-transition: opacity 0.25s, max-width 0.45s;
        -moz-transition: opacity 0.25s, max-width 0.45s;
        -o-transition: opacity 0.25s, max-width 0.45s;
    transition: opacity 0.25s, max-width 0.45s;
                /* Duration fixed since we animate additional hidden width */
    }
    .has-spinner.active {
    cursor:progress;
    }
    .has-spinner.active .spinner {
    display:inline-block;
    opacity: 1;
            max-width: 50px;
            /* More than it will ever come, notice that this affects on animation duration */
    }

.snippet {
	overflow: hidden;
	position: relative;
	border-radius: 5px;
}

.thumbnail {
    margin:0 5px 5px 0px;
    padding:0px;
    background:#ccc;
}

.snippet.tablet {
	width: 150px;
	height: 195px;
}

.snippet.tablet .figure {
	height:150px;
	width:auto;
}
.snippet.tablet .caption {
	height:25px;
        padding:0px 0px;
        position:relative;
         text-overflow: ellipsis;
        display: inline-block;
        white-space: nowrap;
        overflow: hidden;

}
.snippet.tablet .caption .story-footer {
        left:0px;
}

.thumbnail .figure { 
	margin-left: auto;
	margin-right: auto;
	display: table;
	table-layout:fixed;
}

.thumbnail .figure a, .thumbnail .figure span {
	display: table-cell;
	vertical-align: middle;
	max-height: 150px;
	max-width: 150px;
}

.img-polaroid {
    border:none;
}

.thumbnail .figure .img-polaroid {
    padding:0px;
	height:150px; 
 	width: 150px; 
	background-repeat:no-repeat; 
	background-position:center;
    justify-content: center;
    /* margin: 0 auto; */
    object-fit: contain;
    vertical-align: middle;
}

.thumbnail .figure .mouseover, .thumbnail .figure .mouseoverfix {
    padding-left: 0px;
    position: absolute;
    left: 0;
    bottom: 0;
    right: 0;
    background-color: whitesmoke;
    opacity: 0.8;
    height: 100%;
    text-align: justify;
    width: 100%;
}


.thumbnail {
	/*background-color: #ffffff;
	border: 0px solid #bee6d3;
	*/position:relative; 
    float:left;
}

.thumbnail:hover {
	background-color: #fefad5;
}
img.loading {
        background: transparent url(spinner.gif) no-repeat scroll center center;
}
.selectFile {
font-size: 7.2em;
color:#9E9E9E;
cursor:pointer;
left:30px;
}
.selectFile.plus {
    color:white;
    top: -25px;
    left: -43px;
    font-size: 2.2em;
}
.selectFile.active {
    color:#2962FF;
}
.progress-bar {
    text-align:left;
}

/* Mimic table appearance */
div.table {
display: table;
}
div.table .file-row {
display: table-row;
}
div.table .file-row > div {
display: table-cell;
         vertical-align: top;
         border-top: 1px solid #ddd;
padding: 8px;
}
div.table .file-row:nth-child(odd) {
background: #f9f9f9;
}



/* The total progress gets shown by event listeners */
#total-progress {
opacity: 0;
transition: opacity 0.3s linear;
}

/* Hide the progress bar when finished */
#previews .file-row.dz-success .progress {
opacity: 0;
transition: opacity 0.3s linear;
}

/* Hide the delete button initially */
#previews .file-row .delete {
display: none;
}

/* Hide the start and cancel buttons and show the delete button */

#previews .file-row.dz-success .start,
#previews .file-row.dz-success .cancel {
display: none;
}
#previews .file-row.dz-success .delete {
display: block;
}

.dropzone {
background:#fff;
border:2px dashed rgba(0,0,0,0.5);
       box-shadow:0 2px 5px rgba(0,0,0,0.1), inset 0 0 40px rgba(0,0,0,0.1);
       border-radius:2px;
padding:10px;
}

.dropzone.hover {
background:#e3e3e3;
}
.dropzone.dz-clickable { cursor: pointer; }
.dropzone.dz-clickable * { cursor: default; }
.dropzone.dz-clickable .dz-message, .dropzone.dz-clickable .dz-message * { cursor: pointer; }
.dropzone.dz-drag-hover { border-style: solid; }
.dropzone.dz-drag-hover .dz-message { opacity: 0.5; }
  </style>
</head>

<body>

<div class="container submitObs" style="padding: 0px 10px;">
  <div class="row" ng-controller="AppCtrl" ng-init="init(${appSettings})">
    <div id="fileSource" class="col-md-3 col-sm-3" style="padding:0px;position:fixed;">
    <ul class="list-group">
    <li class="list-group-item" ng-click="selectApp('Local')">
    <a >My Computer</a>
    </li>
    <li class="list-group-item"  ng-click="selectApp('Facebook')">
    <a>Facebook</a>
    <a title="logout" ng-if="isConnected('Facebook')" ng-click="logout('Facebook')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Google')">
    <a >Google Drive</a>
    <a title="logout"  ng-if="isConnected('Google')" ng-click="logout('Google')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>

    <li class="list-group-item" ng-click="selectApp('Photos')">
    <a>Google Photos</a>
    <a title="logout"  ng-if="isConnected('Photos')" ng-click="logout('Photos')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>

    <li class="list-group-item" ng-click="selectApp('Flickr')">
    <a >Flickr</a>
    <a title="logout"  ng-if="isConnected('Flickr')" ng-click="logout('Flickr')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Dropbox')">
    <a>Dropbox</a>
    <a title="logout"  ng-if="isConnected('Dropbox')" ng-click="logout('Dropbox')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Wikimedia')">
    <a>Wikimedia</a>
    <a title="logout"  ng-if="isConnected('Wikimedia')" ng-click="logout('Wikimedia')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
    </li>
    <li class="list-group-item" ng-click="selectApp('Youtube')">
    <a >Youtube</a>
    </li>
 
    <!--li class="list-group-item">
          <a ng-click="selectApp('Box')">Box</a>
          <a ng-if="isConnected('Box')" ng-click="logout('Box')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
        </li>
        <li class="list-group-item">
          <a ng-click="selectApp('OneDrive')">OneDrive</a>
          <a ng-if="isConnected('OneDrive')" ng-click="logout('OneDrive')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
        </li>
        <li class="list-group-item">
          <a ng-click="selectApp('AmazonCloudDrive')">Amazon Cloud Drive</a>
          <a ng-if="isConnected('AmazonCloudDrive')" ng-click="logout('AmazonCloudDrive')" class="pull-right"><i class="glyphicon glyphicon-eject"></i></a>
        </li-->
              </ul>
    </div>
    <div class="col-md-9 col-sm-9" style="margin-left:26%; width:73%;">
    <div class="row filesPane">
        <div>
          <div ng-if="app!=undefined ">


            <div class="" style="border-bottom:1px solid #cfcfcf; text-align:center;margin-bottom:10px;">
            <a class="btn btn-sm btn-primary pull-left glyphicon glyphicon-chevron-left" ng-if="showBackButton" ng-click="backButton()" style=""></a>
            <h4>{{app}}</h4>
            </div>


            <div ng-if="!isConnected(app) && runningApp !='Local' && runningApp !='Youtube'" style="text-align: center;">
              <a class="btn btn-primary" ng-click="login(app)">
                Connect {{app=='AmazonCloudDrive'? 'Amazon Cloud Drive' : app}}</a>
            </div>

             <div ng-if="!isConnected(app) && runningApp =='Local'" style="text-align: center;height:227px;line-height:25px;position:relative;overflow:auto;">
             <div id="uploadForm" style="text-align: -moz-center;overflow:auto;position:absolute;height:100%;width:100%;">
                <form id="submitIt" class="upload_resource1 dropzone needsclick dz-clickable dz-started" dropzone local-importing="local-importing" dropzone1="dropzone" method="post"  enctype="multipart/form-data" style="height:100%;"> 
                    <div class="dz-default dz-message">
                        <i class="glyphicon glyphicon-file selectFile" ng-class="{active: hover}" ng-mouseenter="hover = true" ng-mouseleave="hover = false"></i>
                        <i class="glyphicon glyphicon-plus selectFile plus" ng-mouseenter="hover = true" ng-mouseleave="hover = false" ></i>
                        <br/>
                        <span class="lead">Drag and drop or click to select files to upload </span>
                            <br/>
                        or choose from
                        <br/>
                        <a ng-click="selectApp('Facebook')" >Facebook</a>,
                        <a ng-click="selectApp('Google')">Google Drive</a>,
                        <a ng-click="selectApp('Photos')">Google Photos</a>,
                        <a ng-click="selectApp('Flickr')">Flickr</a>,
                        <a ng-click="selectApp('Dropbox')">Dropbox</a>,
                        <a ng-click="selectApp('Wikimedia')">Wikimedia</a>,
                        <a ng-click="selectApp('Youtube')">Youtube</a> 
        
                    </div>

                    <div class="fallback">
                        <input type="file" class="fileUploadInput btn btn-primary" style="display: none;" name="resources" id="fileUploadInput" custom-on-change="uploadFileSelect" accept="image/*|audio/*" title="Choose File" multiple/> 
                    </div>
                </form>           

            </div>
            <div class="table table-striped" class="files" id="previews">

                <div id="template" class="file-row">
                <!-- This is used as the file preview template -->
                <div>
                <span class="preview"><img data-dz-name /></span>
                </div>
                <div>
                <p class="name" data-dz-name></p>
                <strong class="error text-danger" data-dz-errormessage></strong>
                </div>
                <div>
                <p class="size" data-dz-size></p>
                <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0">
                <div class="progress-bar progress-bar-success" style="width:0%;" data-dz-uploadprogress></div>
                </div>
                </div>
                <div>
                <!--button class="btn btn-primary start">
                <i class="glyphicon glyphicon-upload"></i>
                <span>Start</span>
                </button-->
                <button data-dz-remove class="btn btn-warning cancel">
                <i class="glyphicon glyphicon-ban-circle"></i>
                <span>Cancel</span>
                </button>
                <button data-dz-remove class="btn btn-danger delete">
                <i class="glyphicon glyphicon-trash"></i>
                <span>Delete</span>
                </button>
                </div>
                </div>

                </div>


            </div>
            <div ng-if="!isConnected(app) && runningApp =='Youtube'" style="text-align: center; height:227px;">
              <form id="submitVideo" class="form-horizontal upload_resource2" method="post" style="text-align: -moz-center;">                
              <div class="form-group">
              <label  class="col-sm-3 control-label">Enter YouTube watch url like http://www.youtube.com/watch?v=v8HVWDrGr6o</label>
              <div class="col-sm-9">
              <input type="text" class="fileUploadInput form-control" name="videoResources" id="videoFileUploadInput" custom-on-change="uploadVideoFile" title="Please enter youtube video url" placeholder="Add Video">
              </div>
              </div>
              <div class="form-group">
              <div class="col-sm-offset-2 col-sm-10">
              <button type="submit" class="btn btn-default">Submit</button>
              </div>
              </div>
              </form>                
            </div>


            <div ng-if="isConnected(app)">

            <div class="thumbnails clearfix" style="height:227px;overflow-y:auto;">
              <div ng-if="!items" style="text-align: center;">
                Loading...
              </div>
              <div ng-if="items.length == 0" style="text-align: center;">
                No Files or Folders
              </div>
                <div class="list-group filesutraItem"  >
                    <a class="col-sm-2 list-group-item" ng-class-odd="{'color1':toggleObject}" ng-repeat="item in items"  ng-if="item.type != 'file'" ng-click="selectItem(item); toggleObject = !toggleObject"
                       ng-class="itemId.indexOf(item.id) > -1 && item.type != 'folder' ? 'selectedItem' : ''">
                     <i ng-class="item.type == 'file' ? 'glyphicon glyphicon-file' : 'glyphicon glyphicon-folder-open'"></i>
                      {{item.name}}

                      </a>
                  </div>
                  <div class="imgContainer thumbnail" ng-if="item.type == 'file'"  ng-repeat="item in items">
                    
                      <ul ng-click="selectItem(item); toggleObject = !toggleObject" style="padding:0px;margin:0px;list-style:none;">
                        <li class="snippet tablet" ng-class="itemId.indexOf(item.id) > -1 && item.type != 'folder' ? 'selectedItem' : ''" style="height:100%">
                        <div class="figure">
                          <img  ng-if="runningApp != 'Google'" src="{{item.iconurl}}" class="img-responsive img-polaroid loading"/>
                          <img  ng-if="runningApp == 'Google'" src="{{item.thumbnail}}" class="img-responsive img-polaroid loading"/>
                          </div>
                        <div class="caption">
                        {{item.name}}
                        </div>
                       </li>
                      </ul>
                        <button id="singlebutton" name="singlebutton" ng-disabled="isDisabled" style="margin:0 auto;display:block;width:100%;" class="btn btn-primary bottom2" ng-show="$last && showButton" ng-click="gettingList(1)">{{loadMoreText}}</button> 
                  </div>
                  </div>
         
                  
            </div>

            <div ng-if="isConnected(app) || runningApp =='Local' || runningApp =='Youtube'" >
                <div style="clear:both; border-top:1px solid #cfcfcf;padding-top:5px;margin-top:10px;">
                    <a id="importBtn" class="btn btn-primary pull-right import-btn has-spinner" ng-if="app" style="text-align:center;margin-right:0px;" disabled='disabled' ng-click="import($event)"> <span class="spinner"><asset:image src="/all/spinner.gif" absolute="true"/></span> Import</a>
                </div>
    
                <div id="total-progress" class="progress progress-striped active" style="height:35px;margin:0px;display:none;">
                    <div class="progress-bar" style="line-height:35px;"><span>0%</span></div>
                </div>
                <div id="status"></div>
            </div>
          </div>
          
        </div>
             </div>
    </div>
  </div>
  </div>
  <asset:javascript src="application.js"/>

</body>
<script type="text/javascript">
Dropzone.autoDiscover = false;
</script>
</html>
