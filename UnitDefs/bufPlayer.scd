Udef(\bufPlayer, {
	var sf = \soundFile.kr( [ 0, 1, 0 ] );

	var bufp = PlayBuf.ar( 1, sf[0], \bufrate.ukr(1,1/4,4) * BufRateScale.kr(sf[0]), 1, 44100 * \fileStartPos.ukr(0,0,60*60), sf[2] );

	UOut.ar( 0, bufp * \amp.kr(0.5) * \globalAmp.ukr(0.1,0.0,1.0,\amp) )
})
.category_(\ImmLib)
.setSpec(\soundFile, BufSndFileSpec() );