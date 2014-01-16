
//different seed per synth
Udef(\immWhiteNoise, {
	UOut.ar(0, WhiteNoise.ar * \amp.kr(0.1) )
})
