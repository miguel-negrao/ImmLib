(
var x;

ImmDef2(\sphericalHarmonic, { |t, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonic(2,3).(t,f);

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]);

ImmDef2(\sphericalHarmonicPlotted, { |t, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonic(2,3).plot(t,f);

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]);

ImmDef2(\expandContract, { |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.( t,  u0, v0, c, curve );

	( amp: USpecArg( pf1 ) )

}, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]);

ImmDef2(\expandContractPlotted, { |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.plot( t,  u0, v0, c, curve );

	( amp: USpecArg( pf1 ) )

}, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]);


//wave2DSin
x = [\u0, ControlSpec(0,2pi), \v0, [-pi,pi], \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ];

ImmDef2(\wave2DSin, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, false);

	( amp: USpecArg( pf1 ) )

}, 0.1, x );

ImmDef2(\wave2DSinPlotted, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, true);

	( amp: USpecArg( pf1 ) )

}, 0.1, x );

//wave2DSaw

ImmDef2(\wave2DSaw, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, false);

	( amp: USpecArg( pf1 ) )

}, 0.1, x);

ImmDef2(\wave2DSawPlotted, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, true);

	( amp: USpecArg( pf1 ) )

}, 0.1, x);

//continousRandomSpotlight
ImmDef2(\continousRandomSpotlight, { |t, numSecs, curve|

	var pf1 = PField.continousRandomSpotlight(t, numSecs, curve);

	( amp: USpecArg( pf1 ) )

}, 0.1, [\numSecs, ControlSpec(0.5,10,default:2), \curve, ControlSpec(-6,6,default:0) ]);

ImmDef2(\continousRandomSpotlight2, { |t, numSecsLo, numSecsHi, curve|

	var pf1 = PField.continousRandomSpotlight2(t, numSecsLo, numSecsHi, curve);

	( amp: USpecArg( pf1 ) )

}, 0.1, [\numSecsLo, ControlSpec(0.5,10,default:1), \numSecsHi, ControlSpec(0.5,10,default:2), \curve, ControlSpec(-6,6,default:0) ]);

ImmDef2(\randomHills, { |t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB|


	var pf1 = PField.randomHills(t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB);

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\numSecs, ControlSpec( 0.5,10, default: 2),
	\numHills, ControlSpec(1,6,step:1, default:3),
	\sizeA, ControlSpec(default:0.3),
	\sizeB, ControlSpec(default:0.35),
	\bumpSize, ControlSpec( 0.0, 0.5, default: 0.5),
	\heightA, ControlSpec(default:1),
	\heightB, ControlSpec(default:1)
]);

ImmDef2(\randomHills2, { |t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB|

	var pf1 = PField.randomHills2(t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB);

	( amp: USpecArg( pf1 ) )

}, 0.1,
[
	\numSecsLo, ControlSpec( 0.5,10, default: 1),
	\numSecsHi, ControlSpec( 0.5,10, default: 2),
	\numHills, ControlSpec(1,6,step:1, default:3),
	\sizeA, ControlSpec(default:1),
	\sizeB, ControlSpec(default:1),
	\bumpSize, ControlSpec( 0.0, 0.5, default: 0.5),
	\heightA, ControlSpec(default:1),
	\heightB, ControlSpec(default:1)
]
);

ImmDef2(\moveHills, { |t, numSecs, numHills, size, step|

	var pf1 = PField.moveHills(t,  numSecs, numHills, size, step);

	( amp: USpecArg( pf1 ) )

}, 0.1, [\numSecs, ControlSpec( 0.5,10, default: 1), \numHills, ControlSpec(1,5,step:1,default:2), \size, ControlSpec(default:0.4), \step, ControlSpec( 0.0, 2.0, default: 1) ]);
)