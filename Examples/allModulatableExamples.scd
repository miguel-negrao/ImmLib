/*
Index:

Pure:

gradient
gradientF
spotlight
spotlightFixed
barU
barV
sphericalHarmonic
expandContract

pre-evaluated:

wave2D
wave2DSin
wave2DSaw
waveUSin
waveVSin
continousRandomSpotlight
continousRandomSpotlight2
randomHills
randomHills2
moveHills

*/
//"pure" PFIELDS

//Gradient
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u2, v2, a, b, curve|

	var pf1 = PField.gradient.(t, u2, v2, a, b, curve );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,
[
	\u0, \azimuth,
	\v0, \elevation,
	\a, [0,1.0],
	\b,	[0,1.0],
	\curve,	ControlSpec(-6,6,default:0)
]);

q.mod = ImmMod(q.def, [\a, 0, \b, 1, \curve, 0]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//GradientF
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u2, v2, a, b, curve|

	//make our own non linear shape to go from a to b
	var x = Env([ 0.0, 1.0, 0.5, 1.0, 0.0 ], [ 0.25, 0.25, 0.25, 0.25 ], [ -2.2, 2.1, 1.8, -2 ]).asMultichannelArray[0];

	var f = { |c| x.envAt(c) };

	var pf1 = PField.gradientF(f).plot( t, u2, v2, a, b );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,
[
	\u0, \azimuth,
	\v0, \elevation,
	\a, [0,1.0],
	\b,	[0,1.0]
]);

q.mod = ImmMod(q.def, [\a, 0, \b, 1]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//spotlight
PSphere(20).plot

(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u0, v0, c, d|

	var pf1 = PField.spotlight.plot( t, u0, v0, c, d );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,[
	\u0, \azimuth,
	\v0, \elevation,
	\c, [0,1.0],
	\d, [0,1.0]
]);

q.mod = ImmMod(q.def, [\c, 0.5]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//spotlightFixed
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, c, d|

	var pf1 = PField.spotlightFixed(0.0,pi/2).plot( t, c, d );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,[
	\c, [0,1.0],
	\d, [0,1.0]
]);

q.mod = ImmMod(q.def, [\c, 0.5]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//barU
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, wideness|

	var pf1 = PField.barU.plot( t, wideness );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,[
	\wideness, [0.0 ,1.0]
]);

q.mod = ImmMod(q.def, [\wideness, 0.0]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//barV
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, wideness|

	var pf1 = PField.barV.plot( t, wideness );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,[
	\wideness, [0,1.0]
]);

q.mod = ImmMod(q.def, [\c, 0.5]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//sphericalHarmonic
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonic(2,3).plot( t, f );

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1,[
	\freq, [1/10, 4]
]);

q.mod = ImmMod(q.def, [\f, 2]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)


//expandContract
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.plot( t,  u0, v0, c, curve );

	PGridPlot( pf1);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]);

q.mod = ImmMod(q.def);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui;
)




//PRE EVALUATED PFIELDSS





//Wave2DSin
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, true);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\u0, [0,2pi], \v0, [-pi,pi], \l, [0.0, 2.0], \freq, [1/10,2] ]);

q.mod = ImmMod(q.def, [\l, 0.4, \freq, 1]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)

//Wave2DSaw
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, true);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\u0, [0,2pi], \v0, [-pi,pi], \l, [0.0, 2.0], \freq, [1/10,2] ]);

q.mod = ImmMod(q.def, [\l, 0.4, \freq, 1]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)


//Wave2D
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, u0, v0, l, freq|

	//make our own non linear shape to go from a to b
	var x = Env([ 0.0, 1.0, 0.5, 1.0, 0.0 ], [ 0.25, 0.25, 0.25, 0.25 ], [ -2.2, 2.1, 1.8, -2 ]).asMultichannelArray[0];

	var g = { |t| x.envAt(t.mod(1.0)) };

	var pf1 = PField.wave2D(t, u0, v0, l, freq, g, true);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\u0, [0,2pi], \v0, [-pi,pi], \l, [0.0, 2.0], \freq, [1/10,2] ]);

q.mod = ImmMod(q.def, [\l, 0.4, \freq, 1]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui
)


//continousRandomSpotlight
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, numSecs, curve|

	var pf1 = PField.continousRandomSpotlight(t, numSecs, curve);

	PGridPlot( pf1);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\numSecs, [0.5,10], \curve, ControlSpec(-6,6,default:0) ]);

q.mod = ImmMod(q.def, [\numSecs, 2]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui;
)

//continousRandomSpotlight2
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, numSecsLo, numSecsHi, curve|

	var pf1 = PField.continousRandomSpotlight2(t, numSecsLo, numSecsHi, curve);

	PGridPlot( pf1);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\numSecsLo, [0.5,10], \numSecsHi, [0.5,10], \curve, ControlSpec(-6,6,default:0) ]);

q.mod = ImmMod(q.def, [\numSecsLo, 1, \numSecsHi,  4 ]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui;
)

//randomHills
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB|


	var pf1 = PField.randomHills(t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB);

	PGridPlot( pf1);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\numSecs, [0.5,10], \numHills, ControlSpec(1,6,step:1), \sizeA, nil, \sizeB, nil, \bumpSize, [0,0.5], \heightA, nil, \heightB, nil]);

q.mod = ImmMod(q.def, [\numSecs, 4, \numHills, 3, \sizeA, 0.3, \sizeB, 0.5, \bumpSize, 0.5, \heightA, 1.0, \heightB, 1.0]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.gui
)

//randomHills2
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB|

	var pf1 = PField.randomHills2(t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB);

	PGridPlot( pf1);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\numSecsLo, [0.5,10], \numSecsHi, [0.5,10], \numHills, ControlSpec(1,6,step:1), \sizeA, nil, \sizeB, nil, \bumpSize, [0,0.5], \heightA, nil, \heightB, nil]);

q.mod = ImmMod(q.def, [\numSecsLo, 1, \numSecsHi, 4, \numHills, 3, \sizeA, 0.3, \sizeB, 0.5, \bumpSize, 0.5, \heightA, 1.0, \heightB, 1.0]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain
)


//moveHills
(
q = ();
q.m = 20;
q.surface = PSphere(q.m);

q.def = ImmDef({ |t, numSecs, numHills, size, step|

	var pf1 = PField.moveHills(t,  numSecs, numHills, size, step);

	PGridPlot( pf1);

	( amp: USpecArg( pf1 ) )

}, q.surface, 0.1, [\numSecs, [0.5, 4], \numHills, ControlSpec(1,5,step:1), \size, [0.0,1.0], \step, [0.0, 2.0] ]);

q.mod = ImmMod(q.def, [\numSecs, 2, \numHills, 2, \size, 0.4, \step, 0.2 ]);

q.u = [\immWhiteNoise, [\amp: 0.23, \ampLag, 0.1], q.mod];

q.chain = ImmUChain(q.surface, 0, 1, inf, true, q.u).fadeIn_(1).fadeOut_(1);

q.score = ImmUScore(q.surface, q.chain);
q.score.prepareAndStart;
q.chain.gui;
)


