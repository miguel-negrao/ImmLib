Udef(\diskPlayer, {
	var sf = \soundFile.kr( [ 0, 1, 0 ] );

	var out = VDiskIn.ar( 1, sf[0], sf[1] * BufRateScale.kr(sf[0]) , sf[2] );

	UOut.ar( 0, out * \amp.kr(0.5) * \globalAmp.ukr(0.1,0.0,1.0,\amp) )
})
.category_(\ImmLib)
.setSpec(\soundFile, DiskSndFileSpec() );