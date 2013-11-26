Udef(\decorrelate, {

    var in =  UIn.ar(0,1);
    var chain = FFT(LocalBuf(2048,1), in );

    chain = PV_Decorrelate(chain, 1,  1);

    UOut.ar(0, IFFT(chain) );

})