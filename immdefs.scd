/*
		(C)opyright 2013-2015 by Miguel Negrão

    This file is part of ImmLib.

		ImmLib is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

		ImmLib is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImmLib.  If not, see <http://www.gnu.org/licenses/>.
*/

(
var x;

ImmDef(\spotlightForPlane, { |t, u0, v0, c, d|
	var pf1 = PField.spotlight.(Val(0),u0,v0,c,d);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
])
.category_(\plane);

ImmDef(\spotlightInverseForPlane, { |t, u0, v0, c, d|
	var pf1 = PField.spotlightInverse.(Val(0),u0,v0,c,d);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
])
.category_(\plane);

ImmDef(\spotlightForSphere, { |t, u0, v0, c, d|
	var pf1 = PField.spotlight.(Val(0),u0,v0,c,d);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 2pi, \lin, 0, 0.0 ),
	\v0, ControlSpec(-pi/2, pi/2, \lin, 0, 0.0 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\sphere);

ImmDef(\spotlightInverseForPlane, { |t, u0, v0, c, d|
	var pf1 = PField.spotlightInverse.(Val(0),u0,v0,c,d);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 2pi, \lin, 0, 0.0 ),
	\v0, ControlSpec(-pi/2, pi/2, \lin, 0, 0.0 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\plane);

ImmDef(\barU, { |t, wideness|
	var pf1 = PField.barU.(Val(0), wideness);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\wideness, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\universal);

ImmDef(\barV, { |t, wideness|
	var pf1 = PField.barV.(Val(0), wideness);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\wideness, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\universal);

ImmDef(\gradientForPlane, { |t, u, v, a, b, curve|
	var pf1 = PField.gradient.(Val(0), u, v, a, b, curve);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\u, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
]).category_(\plane);

ImmDef(\gradient1DForPlane, { |t, angle, a, b, curve|
	var pf1 = PField.gradient1D.(Val(0), angle, a, b, curve);
	( amp: USpecArg( pf1 ) )
}, 0.1, [
	\angle, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
]).category_(\plane);

ImmDef(\gradientForSphere, { |t, u, v, a, b, curve|
	var pf1 = PField.gradient.(Val(0), u, v, a, b, curve);
	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\u, ControlSpec(0.0, 2pi, \lin, 0, 0.0 ),
	\v, ControlSpec(-pi/2, pi/2, \lin, 0, 0.0 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
]).category_(\sphere);


//ok
ImmDef(\sphericalHarmonicForSphere, { |t, m, l, f|
                                          //  -l <= m <= l
	var pf1 = (T(_,_) <%> m <*> l).switchTo({ |tup|
		var l = tup.at2;
		var m = tup.at1.min(l).max(l.neg);
		PField.sphericalHarmonic(m,l).(t,f)
	}, PField.sphericalHarmonic(2,3).(t,f) );

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	'm', ControlSpec(-4, 4, \lin, 1, 0 ),
	'l', ControlSpec(0, 4, \lin, 1, 0 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]).category_(\sphere);

//ok
ImmDef(\sphericalHarmonicForPlane, { |t, m, l, f|
	var sh = PField.sphericalHarmonicNormalizedFunc;
                                          //  -l <= m <= l
	var pf1 = (T(_,_) <%> m <*> l).switchTo({ |tup|
		var l = tup.at2;
		var m = tup.at1.min(l).max(l.neg);
		sh.(m,l).(t,f)
	});

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	'm', ControlSpec(-4, 4, \lin, 1, 2 ),
	'l', ControlSpec(0, 4, \lin, 1, 2 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]).category_(\plane);
/*
ImmDef(\sphericalHarmonicForPlane, { |t, m, l, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonicNormalized(2,3).(t,f);

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	'm', ControlSpec(-4, 4, \lin, 1, 0 ),
	'l', ControlSpec(0, 4, \lin, 1, 0 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]);
*/
//ok
ImmDef(\sphericalHarmonicPlottedForSphere, { |t, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonic(2,3).plot(t,f);

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]).category_(\sphere);
//ok
ImmDef(\expandContractForSphere, { |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.( t,  u0, v0, c, curve );

	( amp: USpecArg( pf1 ) )

}, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]).category_(\sphere);
//ok
ImmDef(\expandContractPlottedForSphere, { |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.plot( t,  u0, v0, c, curve );

	( amp: USpecArg( pf1 ) )

}, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]).category_(\sphere);


//wave2DSin
//ok
x = [\u0, ControlSpec(0,2pi), \v0, [-pi,pi], \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ];
//ok
ImmDef(\wave2DSinForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, false);

	( amp: USpecArg( pf1 ) )

}, 0.1, x ).category_(\sphere);
//ok
ImmDef(\wave2DSinPlottedForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, true);

	( amp: USpecArg( pf1 ) )

}, 0.1, x ).category_(\sphere);

//wave2DSaw
//ok
ImmDef(\wave2DSawForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, false);

	( amp: USpecArg( pf1 ) )

}, 0.1, x).category_(\sphere);
//ok
ImmDef(\wave2DSawPlottedForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, true);

	( amp: USpecArg( pf1 ) )

}, 0.1, x).category_(\sphere);

x = [\u0, ControlSpec(0,1.0,default:0.5), \v0, ControlSpec(0,1.0,default:0.5), \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ];
//ok
ImmDef(\wave2DSinForPlane, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, false);

	( amp: USpecArg( pf1 ) )

}, 0.1, x ).category_(\plane);

//ok
ImmDef(\planeWaveForPlane, { |t, angle, l, freq|

	var pf1 = PField({ |u,v, t, l|
		sin( 2pi * ( (l*u) - t) )
	}).rotate2D.(t.changeRate(freq), angle.nlin(0,2pi), l);

	( amp: USpecArg( pf1 ) )

}, 0.1,  [\angle, [0,1], \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ]; ).category_(\plane);

//wave2DSaw
//ok
ImmDef(\wave2DSawForPlane, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, false);

	( amp: USpecArg( pf1 ) )

}, 0.1, x).category_(\plane);

//continousRandomSpotlight
//ok
ImmDef(\continousRandomSpotlight, { |t, numSecs, curve|

	var pf1 = PField.continousRandomSpotlight(t, numSecs, curve);

	( amp: USpecArg( pf1 ) )

}, 0.1, [\numSecs, ControlSpec(0.5,10,default:2), \curve, ControlSpec(-6,6,default:0) ]).category_(\universal);

//ok
ImmDef(\continousRandomSpotlight2, { |t, numSecsLo, numSecsHi, curve|

	var pf1 = PField.continousRandomSpotlight2(t, numSecsLo, numSecsHi, curve);

	( amp: USpecArg( pf1 ) )

}, 0.1, [
	\numSecsLo, ControlSpec(0.5,10,default:1),
	\numSecsHi, ControlSpec(0.5,10,default:2),
	\curve, ControlSpec(-6,6,default:0)
]).category_(\universal);

//ok
ImmDef(\randomHills, { |t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB|


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
]).category_(\universal);

//ok
ImmDef(\randomHills2, { |t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB|

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
).category_(\universal);

//ok
ImmDef(\moveHills, { |t, numSecs, numHills, size, step|

	var pf1 = PField.moveHills(t,  numSecs, numHills, size, step, true, \bounce);

	( amp: USpecArg( pf1 ) )

}, 0.1, [\numSecs, ControlSpec( 0.5,10, default: 1), \numHills, ControlSpec(1,5,step:1,default:2), \size, ControlSpec(default:0.4), \step, ControlSpec( 0.0, 2.0, default: 1) ]).category_(\universal);






























//for umap
ImmDef(\UMspotlightForPlane, { |t, u0, v0, c, d|
	var pf1 = PField.spotlight.(Val(0),u0,v0,c,d);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\um_sphere);

ImmDef(\UMspotlightInverseForPlane, { |t, u0, v0, c, d|
	var pf1 = PField.spotlightInverse.(Val(0),u0,v0,c,d);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\um_plane);

ImmDef(\UMspotlightForSphere, { |t, u0, v0, c, d|
	var pf1 = PField.spotlight.(Val(0),u0,v0,c,d);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 2pi, \lin, 0, 0.0 ),
	\v0, ControlSpec(-pi/2, pi/2, \lin, 0, 0.0 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\um_sphere);

ImmDef(\UMspotlightInverseForPlane, { |t, u0, v0, c, d|
	var pf1 = PField.spotlightInverse.(Val(0),u0,v0,c,d);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\u0, ControlSpec(0.0, 2pi, \lin, 0, 0.0 ),
	\v0, ControlSpec(-pi/2, pi/2, \lin, 0, 0.0 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\um_plane);

ImmDef(\UMbarU, { |t, wideness|
	var pf1 = PField.barU.(Val(0), wideness);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\wideness, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\um_universal);

ImmDef(\UMbarV, { |t, wideness|
	var pf1 = PField.barV.(Val(0), wideness);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\wideness, ControlSpec(0.0, 1.0, \lin, 0, 0.5 )
]).category_(\um_universal);

ImmDef(\UMgradientForPlane, { |t, u, v, a, b, curve|
	var pf1 = PField.gradient.(Val(0), u, v, a, b, curve);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\u, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
]).category_(\um_plane);

ImmDef(\UMgradient1DForPlane, { |t, angle, a, b, curve|
	var pf1 = PField.gradient1D.(Val(0), angle, a, b, curve);
	( control: USpecArg( pf1 ) )
}, 0.1, [
	\angle, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
]).category_(\um_plane);

ImmDef(\UMgradientForSphere, { |t, u, v, a, b, curve|
	var pf1 = PField.gradient.(Val(0), u, v, a, b, curve);
	( control: USpecArg( pf1 ) )

}, 0.1, [
	\u, ControlSpec(0.0, 2pi, \lin, 0, 0.0 ),
	\v, ControlSpec(-pi/2, pi/2, \lin, 0, 0.0 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
]).category_(\um_sphere);


//ok
ImmDef(\UMsphericalHarmonicForSphere, { |t, m, l, f|
                                          //  -l <= m <= l
	var pf1 = (T(_,_) <%> m <*> l).switchTo({ |tup|
		var l = tup.at2;
		var m = tup.at1.min(l).max(l.neg);
		PField.sphericalHarmonic(m,l).(t,f)
	}, PField.sphericalHarmonic(2,3).(t,f) );

	( control: USpecArg( pf1 ) )

}, 0.1, [
	'm', ControlSpec(-4, 4, \lin, 1, 0 ),
	'l', ControlSpec(0, 4, \lin, 1, 0 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]).category_(\um_sphere);

//ok
ImmDef(\UMsphericalHarmonicForPlane, { |t, m, l, f|
	var sh = PField.sphericalHarmonicNormalizedFunc;
                                          //  -l <= m <= l
	var pf1 = (T(_,_) <%> m <*> l).switchTo({ |tup|
		var l = tup.at2;
		var m = tup.at1.min(l).max(l.neg);
		sh.(m,l).(t,f)
	});

	( control: USpecArg( pf1 ) )

}, 0.1, [
	'm', ControlSpec(-4, 4, \lin, 1, 2 ),
	'l', ControlSpec(0, 4, \lin, 1, 2 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]).category_(\um_plane);
/*
ImmDef(\UMsphericalHarmonicForPlane, { |t, m, l, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonicNormalized(2,3).(t,f);

	( control: USpecArg( pf1 ) )

}, 0.1, [
	'm', ControlSpec(-4, 4, \lin, 1, 0 ),
	'l', ControlSpec(0, 4, \lin, 1, 0 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]);
*/
//ok
ImmDef(\UMsphericalHarmonicPlottedForSphere, { |t, f|
                                          //  -l <= m <= l
	var pf1 = PField.sphericalHarmonic(2,3).plot(t,f);

	( control: USpecArg( pf1 ) )

}, 0.1, [
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
]).category_(\um_sphere);
//ok
ImmDef(\UMexpandContractForSphere, { |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.( t,  u0, v0, c, curve );

	( control: USpecArg( pf1 ) )

}, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]).category_(\um_sphere);
//ok
ImmDef(\UMexpandContractPlottedForSphere, { |t, u0, v0, c, curve|

	var pf1 = PField.expandContract.plot( t,  u0, v0, c, curve );

	( control: USpecArg( pf1 ) )

}, 0.1, [\u0, [0, 2pi], \v0, ControlSpec(pi/2.neg,pi/2, default:0), \c, nil, \curve, ControlSpec(-6,6,default:0) ]).category_(\um_sphere);


//wave2DSin
//ok
x = [\u0, ControlSpec(0,2pi), \v0, [-pi,pi], \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ];
//ok
ImmDef(\UMwave2DSinForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, false);

	( control: USpecArg( pf1 ) )

}, 0.1, x ).category_(\um_sphere);
//ok
ImmDef(\UMwave2DSinPlottedForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, true);

	( control: USpecArg( pf1 ) )

}, 0.1, x ).category_(\um_sphere);

//wave2DSaw
//ok
ImmDef(\UMwave2DSawForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, false);

	( control: USpecArg( pf1 ) )

}, 0.1, x).category_(\um_sphere);
//ok
ImmDef(\UMwave2DSawPlottedForSphere, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, true);

	( control: USpecArg( pf1 ) )

}, 0.1, x).category_(\um_sphere);

x = [\u0, ControlSpec(0,1.0,default:0.5), \v0, ControlSpec(0,1.0,default:0.5), \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ];
//ok
ImmDef(\UMwave2DSinForPlane, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSin(t, u0, v0, l, freq, false);

	( control: USpecArg( pf1 ) )

}, 0.1, x ).category_(\um_plane);

//ok
ImmDef(\UMplaneWaveForPlane, { |t, angle, l, freq|

	var pf1 = PField({ |u,v, t, l|
		sin( 2pi * ( (l*u) - t) )
	}).rotate2D.(t.changeRate(freq), angle.nlin(0,2pi), l);

	( control: USpecArg( pf1 ) )

}, 0.1,  [\angle, [0,1], \l, ControlSpec(0.0, 2.0,default:1), \freq, ControlSpec(1/10,2,default:0.5) ]; ).category_(\um_plane);

//wave2DSaw
//ok
ImmDef(\UMwave2DSawForPlane, { |t, u0, v0, l, freq|

	var pf1 = PField.wave2DSaw(t, u0, v0, l, freq, false);

	( control: USpecArg( pf1 ) )

}, 0.1, x).category_(\um_plane);

//continousRandomSpotlight
//ok
ImmDef(\UMcontinousRandomSpotlight, { |t, numSecs, curve|

	var pf1 = PField.continousRandomSpotlight(t, numSecs, curve);

	( control: USpecArg( pf1 ) )

}, 0.1, [\numSecs, ControlSpec(0.5,10,default:2), \curve, ControlSpec(-6,6,default:0) ]).category_(\um_universal);

//ok
ImmDef(\UMcontinousRandomSpotlight2, { |t, numSecsLo, numSecsHi, curve|

	var pf1 = PField.continousRandomSpotlight2(t, numSecsLo, numSecsHi, curve);

	( control: USpecArg( pf1 ) )

}, 0.1, [\numSecsLo, ControlSpec(0.5,10,default:1), \numSecsHi, ControlSpec(0.5,10,default:2), \curve, ControlSpec(-6,6,default:0) ]).category_(\um_universal);

//ok
ImmDef(\UMrandomHills, { |t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB|


	var pf1 = PField.randomHills(t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB);

	( control: USpecArg( pf1 ) )

}, 0.1, [
	\numSecs, ControlSpec( 0.5,10, default: 2),
	\numHills, ControlSpec(1,6,step:1, default:3),
	\sizeA, ControlSpec(default:0.3),
	\sizeB, ControlSpec(default:0.35),
	\bumpSize, ControlSpec( 0.0, 0.5, default: 0.5),
	\heightA, ControlSpec(default:1),
	\heightB, ControlSpec(default:1)
]).category_(\um_universal);

//ok
ImmDef(\UMrandomHills2, { |t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB|

	var pf1 = PField.randomHills2(t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB);

	( control: USpecArg( pf1 ) )

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
).category_(\um_universal);

//ok
ImmDef(\UMmoveHills, { |t, numSecs, numHills, size, step|

	var pf1 = PField.moveHills(t,  numSecs, numHills, size, step, true, \bounce);

	( control: USpecArg( pf1 ) )

}, 0.1, [\numSecs, ControlSpec( 0.5,10, default: 1), \numHills, ControlSpec(1,5,step:1,default:2), \size, ControlSpec(default:0.4), \step, ControlSpec( 0.0, 2.0, default: 1) ]).category_(\um_universal);

)
