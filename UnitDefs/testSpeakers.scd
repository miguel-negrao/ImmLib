Udef(\testSpeakers, {
	var auto = Demand.ar(Impulse.ar( \speed.ukr(1,0.5,5) ),0,Dseq(~indxs,inf));
	var manual = Select.kr(\bus.ukr(0,0,~indxs.size,step:1),~indxs);
	var index = Select.kr(\auto.ukr(1, BoolSpec()), [manual, auto]);
	var all = \all.ukr(0, BoolSpec());
	var index2 = Select.kr(all, [index, DC.kr(0)]);
	var amp = \amp.ukr(0.5,0.0,1.0) * 0.01;
	var noises = WhiteNoise.ar(amp.dup(24));
	Out.ar( index , Select.ar( index, noises ) * (1-all) );
	Out.ar( 0 , noises * all );
})