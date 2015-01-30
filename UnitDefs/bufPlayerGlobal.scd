Udef(\bufPlayerGlobal, {
	var sf = \soundFile.kr( [ 0, 1, 0 ] );

	var out = PlayBuf.ar( 1, sf[0], \bufrate.ukr(1,1/4,4) * BufRateScale.kr(sf[0]), 1, 44100 * \fileStartPos.ukr(0,0,60*60), sf[2] );

	UOut.ar( 0, out * \amp.kr(0.5) * \globalAmp.ukr(0.1,0.0,1.0,\amp) )
})
.category_(\ImmLib)
.setSpec(\soundFile, UGlobalBufSndFileArgSpec() )
.nameFunc_({ |unit|
	var sf, path, globalBufSndFile;
	if( (sf = unit.get( \soundFile )).notNil ) {
		if( (globalBufSndFile = UGlobalBufSndFile.get(sf.id)).notNil ) {
			"% : % : bufPlayer".format(sf.id, globalBufSndFile.bufSndFile.path)
		} {
			"% : bufPlayer".format(sf.id)
		};
	} {
		"bufPlayer"
	};
});