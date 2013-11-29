/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
*/

(

Udef(\mynoise7, {

	var out1 = BPF.ar(

		(BrownNoise.ar + WhiteNoise.ar) * 0.2,

		\freq.kr(400,1.0)

	) * Line.ar(0.0,1.0,1);

	var out2 = Dust.ar( \density.ukr(100.0, 0.0, 500.0) ) * 1.0 * EnvGen.ar(Env([0.0,0.0,1.0],[10,10]));

	var out = (  out2 + out1 );

	UOut.ar(0, out )

})

)


(
q = ();

//40
q.m = 40;

q.surface = PSurface.sphere(q.m);

q[\f] = { |t|
	//was the order of argumnts the same as in ~pfFunds ?
	var out = PField.randomHills( t, 8, 1);
	var out2 = PField.randomHills( t, 4);

	//PHemiPlot( out );
	PGridPlot( out );
	//out.immPlot(\grid, q.surface);
	//out.immPlot(\smooth, \sphere);

	(
		density: UArg( out.linexp(0.0,0.5,1.0,100.0) ),
		freq: UArg( out2.linexp(0.0,0.5,100,10000) )
	)
};

q.chain1 = MUChain.immNew(q.surface, [\mynoise7, nil , ImmDef(q[\f], q.surface, 0.1) ] ).fadeIn_(1).fadeOut_(1);

q.score = UScore.immNew(q.surface, q.chain1);
q.score.gui;
q.score.prepareAndStart
)
q.score.gui

UScore.current.asCompileString



/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
*/


/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
2 - DISTORT THOSE SINE WAVES
*/
// Number of harmonics is changing on each region of space
(
Udef(\pitched1, {

	var out = SinOsc.ar(ExpRand(100,300), LinRand(0, 2*pi) );

	out = (out * \amp.ukr(default:0.0, minval:0.0, maxval:1.0, warp: \lin, step:0, lag:0.1) ).clip * 0.1;

	out = EnvGen.ar(Env([0.0,1.0,1.0,0.0],[5,50,5],[3,0,-3])) * out;

	UOut.ar(0, out )

})
)

(
q = ();

q.surface = PSurface.sphere(20);

q[\f] = { |t|

	var out =  PField.randomHills( t, numSecs:5);

	PGridPlot( out);

	( amp: USpecArg( out ) )

};

q.chain1 = MUChain.immNew(q.surface, 0, 0, 60, [\pitched1, nil , ImmDef(q[\f], q.surface, 0.1) ] )
.fadeIn_(10).fadeIn_(10);

q.score = UScore.immNew(q.surface, q.chain1);

q.score.prepareAndStart
)
q.score.gui
/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
DISTORT THOSE SINE WAVES
*/





/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM

3 - FM
*/
// index of modulation
(
Udef(\fm2, {

	//index of modulation
	var i = \i.ukr(10, minval:0, maxval:50, lag:0.2);

	//modulation frequency
	var fm = \fm.ukr(100, minval:1, maxval:200, lag:0.2);

	var out = SinOsc.ar(
		\fc.ukr( 400, \freq, lag: 0.2) + SinOsc.ar( fm, mul: i*fm )
	);


	out = out * 0.3;

	out = EnvGen.ar(Env([0.0,1.0,1.0,0.0],[5,50,5],[3,0,-3])) * out;

	UOut.ar(0, out * \amp.kr )
})
)

//with a line expading the range of the fm modulation.
(
var numSecs = 5;
q = ();
q.surface = PSurface.sphere(40);

q[\f] = { |t|
	var i =  PField.randomHills(t, 4);
	var fm =  PField.randomHills(t, 5);

	PGridPlot( i );
	PHemiPlot( i );

	( i: USpecArg( i * 2 ), fm: UArg( fm.linlin( 0.0, 0.5, 50, 100) ) )

};

q.chain1 = MUChain.immNew(q.surface, [\fm2, [\fc, 200, \amp, 0.1], ImmDef(q[\f], q.surface, 0.1) ] );

q.score = UScore.immNew(q.surface, q.chain1);

q.score.prepareAndStart;

q.score.gui;
)

v  = q.chain1.gui
v.uguis[0].insp

/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
*/











/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
4 - HARMONIC SWEEP, HARMONIC, SWEET.
*/
(
Udef(\blip3, {
	var b = LFNoise1.kr(0.1).range(0.005, 0.06);
	var mix = \mix.ukr(0.0, \unipolar);
	var freq = \freq.kr(100);
	var source = ( Saw.ar(freq) * (1-mix)) + ( mix * BrownNoise.ar );
	var reson = Resonz.ar( source,
		freq: SinOsc.ar(
			ExpRand(0.001,0.005),
			LinRand(0.0, 2*pi)
		).exprange(freq*0.9,freq*40),
		bwr: b
	);
	var out = reson  * \amp.kr(1.0);
	out = 1.2 * out;
	out = EnvGen.ar(Env([0.0,1.0,1.0,0.0],[5,50,5],[3,0,-3])) * out;

	UOut.ar(0, out )
})
)

(
q = ();

q.surface = PSurface.sphere(40);

q[\f] = { |t|

	var pf = PField({ |u, v, t, k|
		var tau = 2*pi;
		(sin ( (v * tau * k) + t ) + 1)/2
	});

	var x = PField.randomHills(t, 10) * t.line(1.0,3.0,60);

	PGridPlot( x );

	( mix: USpecArg( x ) )

};

q.chain1 = MUChain.immNew(q.surface, [ \blip3, [\freq, 60, \amp, 0.6], ImmDef(q[\f], q.surface, 0.1) ] );

q.score = UScore.immNew(q.surface, q.chain1);

q.score.prepareAndStart;

q.score.gui;
)

/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
HARMONIC SWEEP, HARMONIC, SWEET.
*/













/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'

HIGH FREQUENCY BEEPS
*/
(

Udef(\test1, {
	var n = 40;
	var sines = n.collect{ SinOsc.ar( ExpRand(\a.ir(10000), \b.ir(18000)) * LFNoise1.kr(0.1).range(1.0,1.05) ) }.sum / n;
	var out = sines * Lag.ar( LFPulse.ar(ExpRand(15,17) ),  0.001 );
	out = out * 0.6 * \amp.ar(0, 0.1).clip;
	out = out *1.7;
	UOut.ar(0, out )
});

Udef(\test2, {
	var n = 40;
	var sines = n.collect{ SinOsc.ar( ExpRand(\a.ir(8000), \b.ir(9000)) * LFNoise1.kr(0.1).range(1.0,1.05) ) }.sum / n;
	var out = sines * Lag.ar( LFPulse.ar(ExpRand(15,17) ),  0.001 );
	out = out * 0.4 * \amp.ar(0, 0.1).clip * 0.2;
	out =  out*1.7;
	UOut.ar(0, out )
});

Udef(\test3, {
	var n = 40;
	var sines = n.collect{ SinOsc.ar( ExpRand(\a.ir(5000), \b.ir(6000)) * LFNoise1.kr(0.1).range(1.0,1.05) ) }.sum / n;
	var out = sines * Lag.ar( LFPulse.ar(ExpRand(15,17) ),  0.001 );
	out = out * 0.3 * \amp.ar(0, 0.1).clip * 0.2;
	out = out*1.7;
	UOut.ar(0, out )
})
)

//isn't working
(
q = ();

q.surface = PSurface.sphere(20);

//{ |t, surface, numSecs, numHills = 5, size = 0.35|
//1, 6, 0.3, 1.0  - more movement, less smooth
//1, 3, 0.3, 1.0  - even less smoth, more punctual
//1, 10, 0.26, 1.0 - very localized ,
//1, 3, 0.36, 1.0 - smoother
//1, 3, 0.36, 0.1 - very slow change


q[\f] = { |t|

	//var x = PField.moveHills(t, 1, 6, 0.3, 1.0, false);
	//numSecs, numHills = 5, size = 0.4, step = 0.4
	var x = PField.moveHills(t, 1, 5, 0.1, 0.2, false);

	PHemiPlot( x );

	(amp: USpecArg(x) )

};

q.chain1 = MUChain.immNew(q.surface, 0, 0, 60, [ \test1, nil,  ImmDef(q[\f], q.surface, 0.1) ] );
q.chain2 = MUChain.immNew(q.surface, 50, 1, 60, [ \test2, [], ImmDef(q[\f], q.surface, 0.1) ] );
q.chain3 = MUChain.immNew(q.surface, 100, 2, 60, [ \test3, [], ImmDef(q[\f], q.surface, 0.1) ] );

q.score = UScore.immNew(q.surface, q.chain1, q.chain2, q.chain3);

q.score.prepareAndStart;

q.score.gui;
)


)
/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
*/





































/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
*/
//standardL + hills

(
Udef(\standardLSin, {
	var chaos = StandardL.ar(SampleRate.ir / \freqDiv.ukr(2, 1, 10.0, lag: 0.1) , \k.ukr(0.9, 0.9,4, lag: 0.1) ) * 0.3;
	var f = ExpRand(60,100);
	var sin = SinOsc.ar(f) + SinOsc.ar( XLine.ar(f,f+60,120) ) + SinOsc.ar( XLine.ar(f, f+30,120) );
	var out = chaos + (0.2 * sin);
	out = out * Line.ar(0.0,1.0,1);
	//with small delays:
	out = CombC.ar(out, 0.1, 0.1, 3) * 0.2;
	//out = (CombC.ar(out, 0.1, 0.1, 3) * 0.3) + (out * 0.7);

	out = out * 0.25;

	//out = EnvGen.ar( Env([0.0,1.0,1.0,0.0],[10,40,10],[3,0,-3] )) * out;

	UOut.ar(0, out *  \amp.kr(1.0) )
} )
)


(
q = ();

//q.surface = PSurface.sphere(40);
q.surface = PSurface.sphericalGeometry([[0,pi/2]]);

q[\f] = { |t|

	var hills = PField.randomHills(t, 5);

	var a = t.line(0.8, 0.0, 40);

	var b = { |t| Env([2.0,1.5,0.1],[30,30]).at(t) }.lift.(t);

	var hillsScaled = { |x, a, b| x.linlin(0.0,1.0, a, b) }.lift.(hills, a, b);

	PGridPlot( hills );

	( k: USpecArg( hillsScaled ) )
};

q.chain1 = MUChain.immNew(q.surface, [ \standardLSin, [], ImmDef(q[\f], q.surface, 0.1) ] );

q.score = UScore.immNew(q.surface, q.chain1);

q.score.prepareAndStart;

q.score.gui;
)
)
/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
*/


















/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
IMPULSES - what else do you need, anyway ?
II   II   II   II   II   II   II   II   II   II   II   II   II
*/
//0dB Master fader
//IMPULSES
(
Udef(\imp, {
	//var impulseSize = \impulseSize.kr(0.006);
	var impulseSize = EnvGen.ar( Env([0.0001,0.04,0.04,0.0001],[60,10,10], \exponential ));
    var env = EnvGen.ar( Env([0.0,1.0,0.0],impulseSize ! 2), Impulse.ar(\freq.kr(1.0) )  );
    var out = \amp.kr(1.0) * WhiteNoise.ar * 0.1 * env;

	out = EnvGen.ar( Env([0.0,1.0,1.0,0.0],[10,60,10],[3,0,-3] )) * out * 2.5;

    UOut.ar(0, out)
})
.setSpec(\mix, \unipolar.asSpec)
.setSpec(\freq, [0.1,50.0,\exp].asSpec)
.setSpec(\impulseSize, [0.0001,0.02,\exp]);
)

(
q = ();

//25
q.m = 25;

q.surface = PSurface.sphere(q.m);

q[\f] = { |t|
	//I don't think the freq is the same here...
	var field = PField({ |u, v, t| (sin ( (v*2pi*0.2) + t ) +1 )/2});

	var x = field.valuePlot(t);

	PGridPlot( x );

	( freq: UArg( x.linexp(0.0,1.0,0.1,30) ) )

};

q.chain1 = MUChain.immNew(q.surface, [\imp, [\amp, 1.0], ImmDef(q[\f], q.surface, 0.1) ]);

q.score = UScore.immNew(q.surface, q.chain1);

q.score.prepareAndStart

)
q.score.gui
UScore.current.asCompileString

/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
IMPULSES
II   II   II   II   II   II   II   II   II   II   II   II   II
*/

/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
                ITERATIVE ALGORITMHS
*/



(
~makeRule30 = { |tSig, m, initState|
	tSig.inject(initState, {|state, t|

		var array = (0 ! m);
		state.do{ |y,i|
			var a = state[(i-1).wrap(0,m-1)];
			var b = state[i];
			var c = state[(i+1).wrap(0,m-1)];
			switch ( [a,b,c] )
			{[1,1,1] } { array[i] = 0 }
			{[1,1,0] } { array[i] = 0 }
			{[1,0,1] } { array[i] = 0 }
			{[1,0,0] } { array[i] = 1 }
			{[0,1,1] } { array[i] = 1 }
			{[0,1,0] } { array[i] = 1 }
			{[0,0,1] } { array[i] = 1 }
			{[0,0,0] } { array[i] = 0 };
		};

		array

	})
};

Udef(\noiseAgusSinOsc, {
	var n = SelectX.ar(Line.ar(0,1,30), [WhiteNoise.ar,SinOsc.ar(100)]);
	var m = SelectX.ar(EnvGen.ar(Env([0.0,0.0,1.0],[30,10])), [SinOsc.ar(1000), SinOsc.ar(50)]);
	var out = Select.ar(\amp.kr(0,0.0), [n, m]) * 0.02;
	out = EnvGen.ar( Env([0.0,1.0,1.0,0.0],[5,45,10],[3,0,-3] )) * out * 1.9;
	UOut.ar(0,  out  )

});
)
//Rule 30 CA


(
q = ();

//30
q.m = 30;

q.surface = PSurface.sphere(q.m);

q.zeros = 0 ! (q.m-1/2);

q.initState = q.zeros++[1]++q.zeros;

q.initState2 = [0,1]++(0 ! (q.m-2));

q[\f] = { |t|

	var freq = ~makeRule30.(t, q.m, q.initState2 ).postln;

	PGridPlot( freq);

	freq.enDebug("freq");

	( amp: USpecArg( freq ) )

};

q.chain1 = MUChain.immNew(q.surface,
	[\noiseAgusSinOsc, nil , ImmDef(q[\f], q.surface, 0.2)]
);

q.score = UScore.immNew(q.surface, q.chain1);

q.score.prepareAndStart


)
q.score.gui

/*
.-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-.   .-.-
/ / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \ \ / / \
`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'   `-`-'
                ITERATIVE ALGORITMHS
};


