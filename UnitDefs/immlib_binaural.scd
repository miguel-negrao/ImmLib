Udef(\immlib_binaural,{ |bus|
	var angles = \angles.kr([0,0],0.1);

	var c = pi/180;
	var encoder =  FoaEncoderMatrix.newOmni;

	var bufs = 4.collect{ |i| 2.collect{ |j|
		"decoder_buf_%_%".format(i,j).asSymbol.ukr(0,BufSndFileSpec(1,512))
	}};

	var  out = UGlobalEQ.ar( UIn.ar(0,1) );
	out = FoaEncode.ar( out , encoder );
	out = FoaTransform.ar(out, 'push', pi/2, angles[0]*c.neg, angles[1]*c);
	out = Mix.new(
		4.collect({ arg i;
			2.collect({ arg j;
				Convolution2.ar(
					out.at(i),
					bufs.at(i).at(j),
					framesize: 512 //for binaural we can hardcode to 512
				)
			})
		})
	);

	Out.ar(0, out)
},
[[ \angles: 	UnitSpherical(0,0), UnitSphericalSpec()]]
)
