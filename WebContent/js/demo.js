'use strict';
//页面一加载完成就执行
$(function () { 
  var audio = $('.audio').get(0),//语音播放器
    textArea = $('#textArea'); //文本内容

  //为语音选择器添加内容
  Object.keys(VOICES).forEach(function(key) {
    $('<option>', { value : key })
    .appendTo($('.select-voice'))
    .text(VOICES[key]);
  });
  //设置初始文本内容
  function updateSampleText() {
    var lang = $('.select-voice').val().substr(0,5);
    $('#textArea').text(SAMPLE_TEXT[lang]);
  }
  $('.select-voice').change(updateSampleText);
  updateSampleText();

  // IE 和 Safari 不支持此实例语音播放，让speak按钮 无效
  if ($('body').hasClass('ie') || $('body').hasClass('safari')) {
    $('.speak-button').prop('disabled', true);
  }

  if ($('.speak-button').prop('disabled')) {
    $('.ie-speak .arrow-box').show();
  }
  //播放错误信息
  $('.audio').on('error', function () {
    $('.result').hide();
    $('.errorMgs').text('Error processing the request.');
    $('.errorMsg').css('color','red');
    $('.error').show();
  });
 //播放语音
  $('.audio').on('loadeddata', function () {
    $('.result').show();
    $('.error').hide();
  });
  //download点击事件
  $('.download-button').click(function() {
    textArea.focus();
    if (validText(textArea.val())) {
      window.location.href = '?download=true&' + $('form').serialize();
    }
  });

  //speak按钮点击事件
  $('.speak-button').click(function() {
    $('.result').hide();
    audio.pause();

    $('#textArea').focus();
    if (validText(textArea.val())) {
      audio.setAttribute('src','?&' + $('form').serialize());
    }
  });

  //校验文本的输入内容
  function validText(text) {
    $('.error').hide();
    $('.errorMsg').css('color','#5a5a5a');

    if ($.trim(text).length === 0) {
      $('.errorMsg').text('Please enter the text you would like to synthesize in the text window.');
      $('.errorMsg').css('color','#5a5a5a');
      $('.error').show();
      return false;
    }

    if (!containsAllLatin1(text)) {
      $('.errorMsg').text('Language not supported. Please use only ISO 8859 characters');
      $('.error').show();
      return false;
    }

    return true;
  }

  /**
   * Check that the text doesn't contains non latin-1 characters.
   * @param  String  The string to test
   * @return true if the string is latin-1
   */
  function containsAllLatin1(str) {
    return  /^[A-z\u00C0-\u00ff\s?@¿''\.,-\/#!$%\^&\*;:{}=\-_`~()0-9]+$/.test(str);
  }

});
