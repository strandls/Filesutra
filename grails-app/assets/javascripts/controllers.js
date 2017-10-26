var filesutraControllers = angular.module("filesutraControllers", ["filesutraServices"]);

filesutraControllers.controller("AppCtrl", ['$scope', '$http', '$location', "fileService", "authService",
        function($scope, $http, $location, fileService, authService) {
            $scope.toggleObject = true;
            $scope.localImporting = {};
            $scope.localImporting.importing = false;;
            $scope.importing = false;
            $scope.loadMoreText = "Load More";
            $scope.dropzone = {};

            $scope.selectApp = function(app) {
                $scope.runningApp = app;
                $location.path(app);
            }

            $scope.login = function(app) {
                var redirectUrl = '/auth/' + (app == 'AmazonCloudDrive'? 'amazon' : app.toLowerCase());
                if (window.opener) {
                    window.location = redirectUrl;
                } else {
                    var oAuthWndow = window.open(redirectUrl, "Filesutra", "width=800, height=600, top=100, left=300");
                    var interval = window.setInterval(function() {
                        console.log('sdfsdfdf');
                        if (oAuthWndow.location.href.indexOf('picker') != -1) {
                            oAuthWndow.close();
                            location.reload();
                        }
                    }, 1000);
                }
            }

            $scope.logout = function(app) {
                var connectedAppPos = $scope.appSettings.connectedApps.indexOf(app)
                    if (connectedAppPos != -1) {
                        authService.logout(app, function(data) {
                            if (data.success) {
                                $scope.appSettings.connectedApps.splice(connectedAppPos, 1);
                                $location.path(app);
                            }
                        });
                    }
            }

            $scope.isConnected = function(app) {
                if ($scope.appSettings.connectedApps.indexOf(app) != -1) {
                    return true;
                } else {
                    return false;
                }
            }
            
            $scope.uploadFileSelect = function(event){
                $('#importBtn').prop('disabled', false);
                $('#importBtn').removeAttr('disabled');

                $scope.filesSelected = true;

                $scope.uploadFile(event);
                return false;
            }

            $scope.uploadFile = function(event){
                //$('#submitIt').submit();
                var me1 = $('#submitIt');
                var bar = $('.progress-bar');
                var percent = $('.percent');
                var status = $('#status');

                $scope.filesSelected = false;
                me1.ajaxSubmit({
                        url : "/demo/upload",
                        dataType : "json",
                        beforeSend: function() {
                            $('#status').empty();
                            $scope.setProgress(0);
                        },
                        uploadProgress: function(event, position, total, percentComplete) {
                            var percentVal = percentComplete;
                            $scope.setProgress(percentVal);
                        },
                        complete: function(xhr) {
                            var str = '<ul style="text-align:left;">';
                            xhr.responseText = JSON.parse(xhr.responseText);
                            for(var i=0; i<xhr.responseText.length; i++) {
                                console.log(xhr.responseText[i]);
                                str += "<li>"+xhr.responseText[i].originalFilename+" ("+xhr.responseText[i].contentType+" - "+xhr.responseText[i].size+")</li>";
                            }
                            str += "</ul>";
                            status.html(str);
                        },
                        success : function (response, statusText, xhr, form) {
                            var images = response;
                            //console.log(typeof(images))
                            //console.log(images)
                            var message = {
                                type  : 'filesutra',
                                data   :  images
                            }
                            if (window.opener) {
                                window.opener.postMessage(message, '*');
                                window.close();
                            } else {
                                // iframe
                                parent.postMessage(message, '*');
                            }
                            $scope.importing = false;
                            $('#importBtn').prop('disabled', false);
                            $('#importBtn').removeAttr('disabled');

                            $(event.currentTarget).removeClass('active').text('Import');
                        },
                        error : function(xhr, ajaxOptions, thrownError){
                            console.log(thrownError);
                        }
                    });
            }

            $scope.uploadVideoFile = function(event){
                var me1 = $('#submitVideo')
                    me1.ajaxSubmit({
                        url : "/demo/uploadVideo",
                        dataType : "json",

                        success : function (response, statusText, xhr, form) {
                            var images = response.videos;
                            console.log(images);
                            //console.log(typeof(images))
                            //console.log(images)
                            var message = {
                                type  : 'filesutra',
                                contentType : 'videoUrl',
                                data   :  images
                            }
                            if (window.opener) {
                                window.opener.postMessage(message, '*');
                                window.close();
                            } else {
                                // iframe
                                parent.postMessage(message, '*');
                            }
                        },
                        error : function(xhr, ajaxOptions, thrownError){
                            console.log(thrownError);
                        }
                    });
            };


            $scope.selectItem = function (item) {
                if(($scope.importing && item.type != "folder") || $scope.localImporting.importing) {
                    return;
                }
                $('#status').empty();
                $scope.setProgress(0);

                if (item.type == "folder") {
                    $location.path($location.path()+'/'+item.id);
                }else{
                    var addToArray = true;
                    for(var i=0;i<$scope.userGroupId.length;i++){
                        if($scope.userGroupId[i]['id'] == item.id){
                            var index = i;//$scope.userGroupId.indexOf(item.id);
                            if (index > -1) {
                                $scope.userGroupId.splice(index, 1);
                                $scope.itemId.splice(index, 1);
                            }
                            addToArray = false;
                        }
                    }
                    if(addToArray){
                        $scope.itemId.push(item.id);
                        $scope.userGroupId.push(item);
                        console.log($scope.itemId);
                    }

                    if($scope.itemId.length > 0) {
                        $('#importBtn').prop('disabled', false);
                        $('#importBtn').removeAttr('disabled');
                    } else {
                        $('#importBtn').prop('disabled', 'disabled');
                        $('#importBtn').attr('disabled', 'disabled');
                    }
                }

                $scope.selectedItem = item;
            }

            $scope.import = function(e) {
                console.log($(e));
                $scope.importing = true;
                $(e.currentTarget).text('Importing');
                $('#importBtn').prop('disabled', 'disabled');
                $('#importBtn').attr('disabled', 'disabled');

                if($scope.app == 'Local') {
                    $scope.dropzone.processDropzone();
                    $scope.importing = false;
                    return;
                }

                var uploadCount = 0;
                var importedFiles = [];
                $('#status').empty();
                $scope.setProgress(0);
                for(var i=0;i<$scope.userGroupId.length;i++){
                    fileService.import($scope.app, $scope.userGroupId[i], function(data) {
                        //console.log(data);
                        uploadCount++;
                        importedFiles.push(data);
                        if(uploadCount == $scope.userGroupId.length ){
                            console.log(importedFiles);
                            var message = {
                                type  : 'filesutra',
                                data   :  importedFiles
                            }
                            if (window.opener) {
                                window.opener.postMessage(message, '*');
                                window.close();
                            } else {
                                // iframe
                                parent.postMessage(message, '*');
                            }
                            var percentVal = ((uploadCount/$scope.userGroupId.length)*100);
                            $scope.setProgress(percentVal);
                            $scope.importing = false;
                            $('#importBtn').prop('disabled', false);
                            $('#importBtn').removeAttr('disabled');

                            $(e.currentTarget).text('Import');
                            $('.selectedItem').removeClass('selectedClass');
                            $scope.selectedItem = undefined;
                            $scope.itemId.length = 0;
                            $scope.userGroupId.length = 0;
                        } else {
                            var percentVal = ((uploadCount/$scope.userGroupId.length)*100);
                            $scope.setProgress(percentVal);
                        }
                    });
                }
            }


            $scope.init = function(appSettings){
                $scope.appSettings = appSettings;
                $.ajaxSetup({cache:false});
                $(document).on("ajaxStart", function(){
                    console.log('ajaxStart----------------------------');
                    $('.container').addClass('busy');
                }).on("ajaxStop", function(){
                    console.log('ajaxStop----------------------------');
                    $('.container').removeClass('busy');
                });
                $scope.filesSelected = false;
                $scope.dropzone.setProgress = $scope.setProgress;
            }

            $scope.backButton = function(){
                window.history.back();
            }

            $scope.$on("$locationChangeSuccess", function (event, newUrl) {
                console.log(event);
                $scope.gettingList(0);
                if($scope.setProgress) $scope.setProgress(0);
                $('#status').empty();
                $scope.filesSelected = false;
                $('#importBtn').prop('disabled', 'disabled');
                $('#importBtn').attr('disabled', 'disabled');
                console.log($location.search('resType'));
            });

            $scope.gettingList = function(code){
                $scope.showBackButton = false;
                var path = $location.path();
                var chunks = path.split("/");
                var app, folderId;
                console.log(chunks);
                if (chunks.length < 2) {
                    $scope.selectApp("Local");
                    return;
                } else {
                    app = chunks[1];
                    $scope.app = app;
                    $scope.runningApp = app;
                }
                if (chunks.length > 2) {
                    folderId = chunks[chunks.length - 1];
                }
                console.log($scope.app);
                        console.log('*******************');
                        console.log($scope.afterTokenVal);

//                if($scope.app == "Facebook" || $scope.app == "Flickr"){

                    if(code==0){
                        $scope.showButton = false;
                        delete $scope.items;
                        //if($scope.afterTokenVal==undefined)
                            $scope.afterTokenVal = '';

                        if ($scope.isConnected(app)) {
                            $scope.userGroupId = [];
                            $scope.itemId = [];
                            fileService.getItems(app, folderId, $scope.afterTokenVal, function (items) {
                                if (chunks.length > 2) {
                                    $scope.showBackButton = true;
                                }else{
                                    $scope.showBackButton = false;
                                }
                                $scope.items = [];
                                $scope.afterTokenVal = items.afterval;
                                if(items.itemResponse && items.itemResponse.length < 25){
                                    $scope.showButton = false;
                                }else{
                                    $scope.showButton = true;
                                    $scope.isDisabled = false;
                                }
                                console.log( $scope.showButton);
                                if(items.itemResponse) {
                                    for(var i=0; i< items.itemResponse.length;i++){
                                        $scope.items.push(items.itemResponse[i]);
                                    }
                                }
                            });
                        }
                    }else{
                        $scope.loadMoreText = "Loading..."
                            $scope.isDisabled = true;
                        fileService.getItems(app, folderId, $scope.afterTokenVal, function (items) {
                            $scope.loadMoreText = "Load More"
                                $scope.afterTokenVal = items.afterval;
                            if (chunks.length > 2) {
                                $scope.showBackButton = true;
                            }else{
                                $scope.showBackButton = false;
                            }
                            console.log(items);
                            if(items!="error" && items.itemResponse){
                                if(items.itemResponse.length < 25){
                                    $scope.showButton = false;
                                }else{
                                    $scope.showButton = true;
                                    $scope.isDisabled = false;
                                }
                                for(var i=0; i< items.itemResponse.length;i++){
                                    $scope.items.push(items.itemResponse[i]);
                                }
                            }else{
                                $scope.showButton = false;
                            }
                        });
                    }
               /* } else {
                    console.log('else');
                    $scope.showButton = false;
                    if ($scope.isConnected(app)) {
                        console.log('isConnected');
                        $scope.userGroupId = [];
                        $scope.itemId = [];
                        delete $scope.items;
                        $scope.items = null;
                        fileService.getListItems(app, folderId, function (items) {
                            console.log(items);
                            if (chunks.length > 2) {
                                $scope.showBackButton = true;
                            }else{
                                $scope.showBackButton = false;
                            }
                            $scope.items = items.itemResponse;
                        });
                    }
                }*/

            }
            $scope.wikiLogin = function(app) {
                 $("#wikiLoginMsg").html("").hide();
                 $("#wikiFormLogin").submit(function( event ) {
                     var username = $('input#wikiUser').val();
                     var password = $('input#wikiPassword').val();
                     if(username && password) {
                         return;
                     } else {
                        $("#wikiLoginMsg").html("Username and password cannot be empty").show();
                        event.preventDefault();
                     }
                });
            }
            $scope.chooseFile = function() {
                $('#fileUploadInput').trigger('click');
            }
            $scope.setProgress = function(percentVal){
                var bar = $('.progress-bar').last();
                var percent = $('.percent').last();
                var progress = $('.progress').last();
                console.log(bar);
                percentVal = Math.floor(percentVal)+'%';
                bar.width(percentVal);
                percent.html(percentVal);
                bar.css("width", percentVal).find('span').text(percentVal);
                if(percentVal == '0%') {
                    progress.hide();    
                } else {
                    progress.show(); 
                }
            }


        }]);

filesutraControllers.directive('dropzone', function() {
    return {
            scope: { 
                dropzone1 : '=',
                localImporting : '='
            },
        link: function(scope, element, attrs) {
            console.log(scope);
            
            var previewNode = document.querySelector("#template");
            previewNode.id = "";
            var previewTemplate = previewNode.parentNode.innerHTML;
            previewNode.parentNode.removeChild(previewNode);

            var config = {
                url: '/demo/upload',
                paramName: "resources",
                maxThumbnailFilesize: 10,
                uploadMultiple: false,
                previewsContainer: "#previews", // Define the container to display the previews
                previewTemplate : previewTemplate,
                autoProcessQueue: true,
                autoQueue:true,
                thumbnailWidth: 80,
                thumbnailHeight: 80,
                createImageThumbnails:true,
                parallelUploads:20,
                acceptedFiles:'image/*,audio/*'
            };

            var eventHandlers = {
                'drop':function() {
                                },
                'addedfile': function(file) {
                    $('#uploadForm').hide();
                    $('#previews').show();

//                  file.previewElement = Dropzone.createElement(this.options.previewTemplate);
                    //file.previewElement.querySelector(".start").onclick = function() { dropzone.enqueueFile(file); };
                    //$('#importBtn').prop('disabled', false).removeAttr('disabled').removeClass('active').text('Import');
                    scope.filesSelected = true;
                    scope.localImporting.importing = true;

                    file.previewElement.querySelector('.cancel').onclick = function() {
                        if(dropzone.files.length == 0) {
                            scope.internalControl.resetDropzone();
                        }
                    };
                },
                'totaluploadprogress':function(event, progress, bytesSent) {
                    document.querySelector("#total-progress .progress-bar").style.width = progress + "%";
                    /*if(progress==100) {
                        $('#importBtn').prop('disabled', false);
                        $('#importBtn').removeAttr('disabled');
                        scope.filesSelected = true;
                    }*/
                },
                'uploadprogress':function(event, progress, bytesSent) {
                    //scope.internalControl.setProgress(progress);
                },
                'processing':function(file) {
                },
                'complete': function(event) {
                 },  
                'success': function (event, response) {
                    if(scope.images == undefined) scope.images = [];
                        scope.images.push(response[0]);
                },
                'queuecomplete':function(event) {
                    //this.options.autoProcessQueue = false;
                    document.querySelector("#total-progress").style.opacity = "0";

                    //dropzone.disable();
                    var images = scope.images;
                    var message = {
                        type  : 'filesutra',
                        data   :  images
                    }
                    if (window.opener) {
                        window.opener.postMessage(message, '*');
                        window.close();
                    } else {
                        // iframe
                        parent.postMessage(message, '*');
                    }
                    $('#importBtn').prop('disabled', false).removeAttr('disabled').removeClass('active').text('Import');
                    //$('#status').html('');
                    scope.localImporting.importing = false;
                    scope.internalControl.resetDropzone();

                }, 
                'error':function(event, errorMessage, xhr) {
                    console.log(event);
                    console.log(errorMessage);
                }

            };
            scope.localImporting = scope.localImporting || {};
            scope.internalControl = scope.dropzone1 || {};
            dropzone = new Dropzone(element[0], config);
            angular.forEach(eventHandlers, function(handler, event) {
                dropzone.on(event, handler);
            });
            scope.internalControl.processDropzone = function() {
                var addedfiles = dropzone.getFilesWithStatus(Dropzone.ADDED);
                if(addedfiles.length > 0) {
                    dropzone.enqueueFiles(addedfiles);
                    dropzone.processQueue();
                } else {
                    scope.internalControl.resetDropzone();
                }
            };
            scope.internalControl.resetDropzone = function() {
                scope.images = [];
                dropzone.removeAllFiles();
                scope.filesSelected = false;
                //dropzone.enable();
                $('#uploadForm').show();
                $('#previews').show();
                $('#importBtn').prop('disabled', false).removeAttr('disabled').removeClass('active').text('Import');
            }

        }
    }
});

