$(function(){
    $('.addreviewform .showaddreviewformlink').click(
      function(){
            $(this).hide();
            $('.addreviewform .hideaddreviewformlink').show();
            
            $('.addreviewform .reviewform').show();
            return false;
        }
    );
    
    $('.addreviewform .hideaddreviewformlink').click(
      function(){
            $(this).hide();
            $('.addreviewform .showaddreviewformlink').show();
            $('.addreviewform .reviewform').hide();
            return false;
        }
    );
    
    $('.paymentform .showpaymentformlink').click(
    	      function(){
    	            $(this).hide();
    	            $('.paymentform .hidepaymentformlink').show();
    	            $('.paymentform .paymentpagelink').show();
    	            $('.paymentform .paymentoptionsform').show();
    	            return false;
    	        }
    	    );
    	    
    $('.paymentform .hidepaymentformlink').click(
      function(){
            $(this).hide();
            $('.paymentform .showpaymentformlink').show();
            $('.paymentform .paymentpagelink').hide();
            $('.paymentform .paymentoptionsform').hide();
            return false;
        }
    );

});