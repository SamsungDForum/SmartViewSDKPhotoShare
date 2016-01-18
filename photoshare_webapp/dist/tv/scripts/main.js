$(function(){

    "use strict";

    var prevImage;
    var imgContainer = $('#imgContainer');
    var EXIF = window.EXIF;

    window.msf.local(function(err, service){

        var channel = service.channel('com.samsung.multiscreen.photoshare');

        channel.connect({name: 'TV'}, function (err) {
            if(err) return console.error(err);
        });

        channel.on('showPhoto',function(msg, from, payload){
            showPhoto(payload);
        });

    });

    function showPhoto(abPhoto){

        var photoBlob = new Blob([abPhoto]);
        var URL = window.URL || window.webkitURL;
        var url = URL.createObjectURL(photoBlob);

        var img = $('<img>');
        img.attr('src', url);
        img.one('load',function(){

            var el = img.get(0);

            EXIF.getData(el, function() {

                console.log(EXIF.pretty(this));

                var o = EXIF.getTag(this,'Orientation');

                var tMap = [];
                tMap[2] = 'rotate3d(0, 1, 0, 180deg)';
                tMap[3] = 'rotate3d(0, 0, 1, 180deg)';
                tMap[4] = 'rotate3d(1, 0, 0, 180deg)';
                tMap[5] = 'rotate3d(1, 1, 0, 180deg)';
                tMap[6] = 'rotate3d(0, 0, 1, 90deg)';
                tMap[7] = 'rotate3d(1, -1, 0, 180deg)';
                tMap[8] = 'rotate3d(0, 0, -1, 90deg)';

                if(tMap[o]) $(this).css('transform',tMap[o]);


            });


            imgContainer.removeClass('waiting');

            var newImg = $(this);
            if(prevImage){
                prevImage.fadeOut(400,function(){
                    $(this).remove();
                    newImg.fadeIn(400);
                    prevImage = newImg;
                });
            }else{
                newImg.fadeIn(400);
                prevImage = newImg;
            }

        });
        img.appendTo(imgContainer);
    }

});





