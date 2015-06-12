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
var x, f, specs;

var make = { |name, f, specs, cat|
	ImmDef(name.asSymbol, f.(\amp), 0.1, specs ).category_(cat.asSymbol);
	ImmDef(("um_"++name).asSymbol, f.(\control), 0.1, specs ).category_(("um_"++cat).asSymbol)
};

//***************************************************************************
specs = [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\c, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\d, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\plot, ControlSpec(0,1,step:1,default:0)
];

f = { |key|
	{ |t, u0, v0, c, d, plotSignal|
		var surface = ImmDef.currentSurface;
		var ua = surface.manifold.rangeU[0];
		var ub = surface.manifold.rangeU[1];
		var va = surface.manifold.rangeV[0];
		var vb = surface.manifold.rangeV[1];
		var u0conv = u0.collect( _.linlin(0.0,1.0,ua,ub) );
		var v0conv = v0.collect( _.linlin(0.0,1.0,va,vb) );

		var pf1 = PField.spotlight.plotOnOffStatic(plotSignal, u0conv,v0conv,c,d);
		Dictionary.newFrom([key, USpecArg( pf1 )])

}};
make.("spotlight", f, specs, "default");

f = { |key|
	{ |t, u0, v0, c, d, plotSignal|
		var surface = ImmDef.currentSurface;
		var ua = surface.manifold.rangeU[0];
		var ub = surface.manifold.rangeU[1];
		var va = surface.manifold.rangeV[0];
		var vb = surface.manifold.rangeV[1];
		var u0conv = u0.collect( _.linlin(0.0,1.0,ua,ub) );
		var v0conv = v0.collect( _.linlin(0.0,1.0,va,vb) );

		var pf1 = PField.spotlightInverse.plotOnOffStatic(plotSignal, u0conv,v0conv,c,d);
		Dictionary.newFrom([key, USpecArg( pf1 )])

	}
};
make.("spotlightInverse", f, specs, "default");

//***************************************************************************
specs = [
	\wideness, ControlSpec(0.0, 1.0,\lin, 0, 0.5 ),
	\plot, ControlSpec(0,1,step:1,default:0)
];

f = { |key| { |t, wideness, plotSignal|
	var pf1 = PField.barU.plotOnOffStatic(plotSignal, wideness);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}};
make.("barU", f, specs, "default");

f = { |key| { |t, wideness, plotSignal|
	var pf1 = PField.barV.plotOnOffStatic(plotSignal, wideness);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}};
make.("barV", f, specs, "default");

//***************************************************************************
//gradient

specs = [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 ),
	\plot, ControlSpec(0,1,step:1,default:0)
];

f = { |key|
	{ |t, u0, v0, a, b, curve, plotSignal|
		var surface = ImmDef.currentSurface;
		var ua = surface.manifold.rangeU[0];
		var ub = surface.manifold.rangeU[1];
		var va = surface.manifold.rangeV[0];
		var vb = surface.manifold.rangeV[1];
		var u0conv = u0.collect( _.linlin(0.0,1.0,ua,ub) );
		var v0conv = v0.collect( _.linlin(0.0,1.0,va,vb) );
		var pf1 = PField.gradient.plotOnOffStatic( plotSignal, u0conv, v0conv, a, b, curve);
		Dictionary.newFrom([key, USpecArg( pf1 )])
}};

make.("gradient", f, specs, "default");


//***************************************************************************

specs =  [
	\angle, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\a, ControlSpec(0.0, 1.0, \lin, 0, 0.0 ),
	\b, ControlSpec(0.0, 1.0, \lin, 0, 1.0 ),
	\curve, ControlSpec(-6.0, 6.0, \lin, 0, 0.0 )
];

f = { |key|
	{ |t, angle, a, b, curve|
		var pf1 = PField.gradient1D.(Val(0), angle, a, b, curve);
		Dictionary.newFrom([key, USpecArg( pf1 )])

	}
};

make.("gradient1DPlane", f, specs, "default");


//***************************************************************************

specs = [
	'm', ControlSpec(-4, 4, \lin, 1, 0 ),
	'l', ControlSpec(0, 4, \lin, 1, 0 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 )
];

f = { |plot| { |key|
	{ |t, m, l, f|
		var surface = ImmDef.currentSurface;
		var plotFunc = if(plot){ PSmoothPlot.reusable }{ { |pf...args| pf.(*args) } };

		//  -l <= m <= l
		var sh = if( surface.isKindOf(PSphericalSurface) ) {
			PField.sphericalHarmonicFunc
		}{
			PField.sphericalHarmonicNormalizedFunc
		};

		var pf1 = (T(_,_) <%> m <*> l).switchTo({ |tup|
			var l = tup.at2;
			var m = tup.at1.min(l).max(l.neg);
			plotFunc.( sh.(m,l) ,t, f)
		}, plotFunc.( sh.(1,1) ,t, f));

		Dictionary.newFrom([key, USpecArg( pf1 )])


}}};
/*
specs = [
	'm', ControlSpec(-4, 4, \lin, 1, 0 ),
	'l', ControlSpec(0, 4, \lin, 1, 0 ),
	\freq, ControlSpec(1/10, 4, \lin, 0, 0.5 ),
	\plot, ControlSpec(0,1,step:1,default:0)
];

f = { |key|
	{ |t, m, l, f, plotSignal|
		var surface = ImmDef.currentSurface;
		var plotFunc = PField.plotOnOffFunc(plotSignal);
		//  -l <= m <= l
		var sh = if( surface.isKindOf(PSphericalSurface) ) {
			PField.sphericalHarmonicFunc
		}{
			PField.sphericalHarmonicNormalizedFunc
		};

		var pf1 = (T(_,_) <%> m <*> l).switchTo({ |tup|
			var l = tup.at2;
			var m = tup.at1.min(l).max(l.neg);
			plotFunc.( sh.(m,l) ,t, f)
		}, plotFunc.( sh.(1,1) ,t, f));

		Dictionary.newFrom([key, USpecArg( pf1 )])


}};
*/
make.("sphericalHarmonic", f.(false), specs, "default");
make.("sphericalHarmonicPlot", f.(true), specs, "smooth_plot");

//***************************************************************************

specs = [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\c, [0,1],
	\curve, ControlSpec(-6,6,default:0),
	\plot, ControlSpec(0,1,step:1,default:0);

];

f = { |plot| { |key|
	{ |t, u0, v0, c, curve, plotSignal|
		var surface = ImmDef.currentSurface;
		var ua = surface.manifold.rangeU[0];
		var ub = surface.manifold.rangeU[1];
		var va = surface.manifold.rangeV[0];
		var vb = surface.manifold.rangeV[1];
		var u0conv = u0.collect( _.linlin(0.0,1.0,ua,ub) );
		var v0conv = v0.collect( _.linlin(0.0,1.0,va,vb) );
		var pf1 = PField.expandContract.plotOnOffStatic(plotSignal, u0conv, v0conv, c, curve );
		Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("expandContract", f.(false), specs, "default");

//***************************************************************************
//wave2DSin
//ok
specs = [
	\u0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\v0, ControlSpec(0.0, 1.0, \lin, 0, 0.5 ),
	\l, ControlSpec(0.0, 2.0,default:1),
	\freq, ControlSpec(1/10,2,default:0.5)
];

f = { |plot| {|key| { |t, u0, v0, l, freq|
	var surface = ImmDef.currentSurface;
	var ua = surface.manifold.rangeU[0];
	var ub = surface.manifold.rangeU[1];
	var va = surface.manifold.rangeV[0];
	var vb = surface.manifold.rangeV[1];
	var u0conv = u0.collect( _.linlin(0.0,1.0,ua,ub) );
	var v0conv = v0.collect( _.linlin(0.0,1.0,va,vb) );
	var pf1 = PField.wave2DSin(t, u0conv, v0conv, l, freq, plot);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("wave2DSin", f.(false), specs, "default");
make.("wave2DSinPlot", f.(true), specs, "smooth_plot");

f = { |plot| {|key| { |t, u0, v0, l, freq|
	var surface = ImmDef.currentSurface;
	var ua = surface.manifold.rangeU[0];
	var ub = surface.manifold.rangeU[1];
	var va = surface.manifold.rangeV[0];
	var vb = surface.manifold.rangeV[1];
	var u0conv = u0.collect( _.linlin(0.0,1.0,ua,ub) );
	var v0conv = v0.collect( _.linlin(0.0,1.0,va,vb) );
	var pf1 = PField.wave2DSaw(t, u0conv, v0conv, l, freq, plot);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("wave2DSaw", f.(false), specs, "default");
make.("wave2DSawPlot", f.(true), specs, "smooth_plot");

//***************************************************************************
//continousRandomSpotlight

specs = [\numSecs, ControlSpec(0.5,10,default:2), \curve, ControlSpec(-6,6,default:0) ];

f = { |plot| {|key| { |t, numSecs, curve|
	var pf1 = PField.continousRandomSpotlight(t, numSecs, curve, plot);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("continuousRandomSpotlight", f.(false), specs, "default");
make.("continuousRandomSpotlightPlot", f.(true), specs, "smooth_plot");

specs = [
	\numSecsLo, ControlSpec(0.5,10,default:1),
	\numSecsHi, ControlSpec(0.5,10,default:2),
	\curve, ControlSpec(-6,6,default:0)
];

f = { |plot| {|key| { |t, numSecsLo, numSecsHi, curve|
	var pf1 = PField.continousRandomSpotlight2(t, numSecsLo, numSecsHi, curve, plot);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("continousRandomSpotlight2", f.(false), specs, "default");
make.("continousRandomSpotlight2Plot", f.(true), specs, "smooth_plot");

//***************************************************************************
//randomHills

specs = [
	\numSecs, ControlSpec( 0.5,10, default: 2),
	\numHills, ControlSpec(1,6,step:1, default:3),
	\sizeA, ControlSpec(default:0.3),
	\sizeB, ControlSpec(default:0.35),
	\bumpSize, ControlSpec( 0.0, 0.5, default: 0.5),
	\heightA, ControlSpec(default:1),
	\heightB, ControlSpec(default:1)
];

f = { |plot| {|key| { |t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB|
	var pf1 = PField.randomHills(t, numSecs, numHills, sizeA, sizeB, bumpSize, heightA, heightB, plot);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("randomHills", f.(false), specs, "default");
make.("randomHillsPlot", f.(true), specs, "smooth_plot");

specs = [
	\numSecsLo, ControlSpec( 0.5,10, default: 1),
	\numSecsHi, ControlSpec( 0.5,10, default: 2),
	\numHills, ControlSpec(1,6,step:1, default:3),
	\sizeA, ControlSpec(default:1),
	\sizeB, ControlSpec(default:1),
	\bumpSize, ControlSpec( 0.0, 0.5, default: 0.5),
	\heightA, ControlSpec(default:1),
	\heightB, ControlSpec(default:1)
];

f = { |plot| {|key| { |t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB|
	var pf1 = PField.randomHills2(t, numSecsLo, numSecsHi, numHills, sizeA, sizeB, bumpSize, heightA, heightB, plot);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}}};

make.("randomHills2", f.(false), specs, "default");
make.("randomHills2Plot", f.(true), specs, "smooth_plot");

//***************************************************************************
//moveHills

specs = [
	\numSecs, ControlSpec( 0.5,10, default: 1),
	\numHills, ControlSpec(1,5,step:1,default:2),
	\size, ControlSpec(default:0.4),
	\step, ControlSpec( 0.0, 2.0, default: 1)
];

f = {|key|  { |t, numSecs, numHills, size, step|
	var pf1 = PField.moveHills(t,  numSecs, numHills, size, step, true, \bounce);
	Dictionary.newFrom([key, USpecArg( pf1 )])
}};

make.("moveHills", f, specs, "default");

)
