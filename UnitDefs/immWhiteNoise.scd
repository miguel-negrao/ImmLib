
//different seed per synth
Udef(\immWhiteNoise, {
	UOut.ar(0, WhiteNoise.ar * \amp.kr(0.1, \ampLag.kr(0.1) ) * \globalAmp.ukr(1.0,\amp) )
}).category_(\ImmLib)
