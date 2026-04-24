package org.oredredging.client.render.block;

import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.AnimationHelper;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;

public class BlockAnimations {
        public static final Animation AMASS = Animation.Builder.create(4.0F).looping()
            .addBoneAnimation("all", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(0.0F, -90.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, -180.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, -267.5F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(0.0F, -357.5F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("all", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .build();

    public static final Animation ERUPT = Animation.Builder.create(3.0F)
            .addBoneAnimation("all", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.125F, AnimationHelper.createRotationalVector(0.9932F, -11.2066F, -5.0974F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.375F, AnimationHelper.createRotationalVector(-2.6244F, -33.6354F, 2.3673F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.5833F, AnimationHelper.createRotationalVector(-9.2496F, -52.4236F, 1.5782F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.7917F, AnimationHelper.createRotationalVector(2.7378F, -71.2531F, -6.9857F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(0.0F, -90.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.2083F, AnimationHelper.createRotationalVector(0.0F, -106.25F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(0.0F, -180.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.0F, AnimationHelper.createRotationalVector(0.0F, -180.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("all", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up1", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(-22.5F, -37.5F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-30.7095F, -35.0658F, 5.4198F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(-19.9226F, -41.5387F, -11.8751F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(-15.1395F, -53.1032F, -4.819F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(-42.0842F, -63.2704F, 9.1632F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(-32.5082F, -60.7973F, -1.5459F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(-25.597F, -58.5606F, -3.7595F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(-22.5398F, -53.9932F, -7.4319F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(-6.0307F, -61.7135F, -11.7785F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.5F, AnimationHelper.createRotationalVector(-18.5307F, -61.7135F, -11.7785F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up1", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, 1.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up4", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(-24.7851F, -38.8447F, 36.3602F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-27.9835F, -40.0526F, 33.5254F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(-34.5504F, -34.8441F, 44.2941F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(-27.0443F, -24.457F, 30.5595F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(-28.8994F, -31.0838F, 34.5331F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(-27.1083F, -33.3163F, 35.863F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(-29.0179F, -35.4757F, 30.3964F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(-32.6762F, -38.4898F, 36.2907F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(-37.4189F, -47.8514F, 36.3919F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.5F, AnimationHelper.createRotationalVector(-55.959F, -17.8092F, 68.8176F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up4", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, 1.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up2", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(22.5F, 42.5F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(21.6946F, 40.8209F, 6.3061F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(25.5175F, 38.8165F, 12.2719F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(22.7744F, 43.2056F, -3.2718F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(19.3312F, 31.5287F, -8.9128F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(23.7341F, 29.2454F, -10.093F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(19.9827F, 28.2141F, -7.4958F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(19.1318F, 35.2269F, -4.3656F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(17.4841F, 43.9136F, -7.4577F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.5F, AnimationHelper.createRotationalVector(32.4841F, 43.9136F, -7.4577F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up2", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createTranslationalVector(-1.0F, 0.0F, -1.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up3", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(32.7324F, 32.7978F, 49.8793F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(24.7157F, 35.0091F, 44.4748F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(22.8814F, 39.4565F, 33.211F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(19.0479F, 23.0897F, 25.9087F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(17.9919F, 13.6048F, 22.566F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(17.3332F, 11.981F, 19.346F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(19.3099F, 12.715F, 16.8996F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(19.8234F, 11.8776F, 19.3105F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(27.7463F, 15.2855F, 29.6297F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.5F, AnimationHelper.createRotationalVector(30.1313F, 9.2173F, 40.8195F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up3", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createTranslationalVector(1.0F, 0.0F, -1.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("core", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(0.0F, -90.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow4", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(0.0F, -10.0F, 7.5F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(-9.8547F, 2.3143F, 5.3443F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(-9.1303F, 4.3838F, 17.6937F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(-8.3136F, -11.1898F, 15.0564F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(-12.2166F, -15.409F, 18.7834F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(-11.8673F, -20.8197F, 17.3104F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(-8.575F, -14.7649F, 8.1774F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow2", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(5.1208F, -12.4517F, -1.1069F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(2.2623F, -13.2599F, 11.6891F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(12.2623F, -13.2599F, 11.6891F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(16.9346F, -44.7885F, 2.4362F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(9.4346F, -44.7885F, 2.4362F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(4.4507F, -45.3938F, 9.4688F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(8.1553F, -34.9049F, 4.4253F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow3", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(0.0F, -22.5F, -10.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(4.1141F, -22.1399F, -20.8052F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(-8.3859F, -22.1399F, -20.8052F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(-5.8859F, -22.1399F, -20.8052F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.125F, AnimationHelper.createRotationalVector(-7.8768F, -21.5404F, -15.4572F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(-8.8442F, -21.1768F, -12.8013F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(-9.9634F, -23.2349F, -9.6967F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow1", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(0.0F, -5.0F, -7.5F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(15.1694F, -9.8279F, -8.8118F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(16.429F, -24.2682F, -13.0723F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.75F, AnimationHelper.createRotationalVector(19.4785F, -21.9525F, -20.83F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createRotationalVector(16.9785F, -21.9525F, -20.83F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.25F, AnimationHelper.createRotationalVector(14.9722F, -23.3297F, -15.6216F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.375F, AnimationHelper.createRotationalVector(12.2276F, -29.9536F, -8.7876F), Transformation.Interpolations.LINEAR)
            ))
            .build();

    public static final Animation BUDDING = Animation.Builder.create(4.0F).looping()
            .addBoneAnimation("all", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.125F, AnimationHelper.createRotationalVector(2.5F, -11.1725F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.1667F, AnimationHelper.createRotationalVector(4.4358F, -14.9001F, 0.5762F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.3333F, AnimationHelper.createRotationalVector(-0.321F, -29.8106F, 2.881F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.4167F, AnimationHelper.createRotationalVector(-7.6605F, -37.2503F, 1.4405F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.5F, AnimationHelper.createRotationalVector(0.0F, -44.69F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.8333F, AnimationHelper.createRotationalVector(-8.9276F, -74.2862F, 9.2683F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.875F, AnimationHelper.createRotationalVector(14.6271F, -77.7535F, -14.8096F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.9167F, AnimationHelper.createRotationalVector(19.4321F, -81.4834F, -14.6122F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0F, AnimationHelper.createRotationalVector(16.4069F, -88.9433F, -14.2173F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-161.5488F, -83.5968F, 166.1777F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.125F, AnimationHelper.createRotationalVector(-164.241F, -87.5097F, 163.8037F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.25F, AnimationHelper.createRotationalVector(22.8999F, -80.7516F, -23.3182F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.2917F, AnimationHelper.createRotationalVector(-4.2879F, -85.2992F, 6.4778F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5417F, AnimationHelper.createRotationalVector(-182.6823F, -59.5907F, 184.5456F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5833F, AnimationHelper.createRotationalVector(-177.086F, -64.64F, 181.4177F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.0417F, AnimationHelper.createRotationalVector(-250.0734F, -310.1193F, -116.9133F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.125F, AnimationHelper.createRotationalVector(-200.6041F, -323.1978F, -95.0982F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.2083F, AnimationHelper.createRotationalVector(-185.433F, -351.8551F, -92.2582F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.3333F, AnimationHelper.createRotationalVector(-167.627F, -335.6137F, -73.9669F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.5F, AnimationHelper.createRotationalVector(-128.7939F, -350.2024F, -67.7875F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.5833F, AnimationHelper.createRotationalVector(-107.3079F, -368.1368F, -51.2986F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(0.0F, -357.5F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("all", new Transformation(Transformation.Targets.TRANSLATE,
                    new Keyframe(0.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.5F, AnimationHelper.createTranslationalVector(0.0F, 0.25F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(2.0F, AnimationHelper.createTranslationalVector(0.0F, 1.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createTranslationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up1", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(-7.3873F, -1.2988F, -9.9162F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.75F, AnimationHelper.createRotationalVector(-2.4914F, -1.0858F, -4.9196F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(-4.9394F, -1.3022F, -7.4108F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-5.0338F, -0.8673F, -2.4288F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.125F, AnimationHelper.createRotationalVector(-0.0975F, 9.0938F, -3.3129F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(-0.0969F, 6.5938F, -3.3087F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.4807F, 6.577F, 1.7245F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.4167F, AnimationHelper.createRotationalVector(2.9614F, -0.9129F, 1.3356F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.5F, AnimationHelper.createRotationalVector(2.9984F, -0.7829F, -1.1613F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.75F, AnimationHelper.createRotationalVector(4.1401F, 6.0808F, 9.2757F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(2.9995F, 1.7137F, -1.0305F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up4", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(-4.9574F, 0.6518F, 7.4718F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.625F, AnimationHelper.createRotationalVector(-4.9713F, -4.3295F, 7.9046F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.75F, AnimationHelper.createRotationalVector(-2.0757F, -4.7458F, 2.9062F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(-4.9819F, -4.5469F, 7.9188F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-4.3324F, 2.321F, -0.1506F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(-1.8324F, 2.321F, -0.1506F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(-2.0277F, 2.1525F, -5.1515F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.625F, AnimationHelper.createRotationalVector(-4.2442F, 2.3995F, 2.336F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.3333F, AnimationHelper.createRotationalVector(-3.7639F, 3.0995F, 12.3231F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(0.7558F, 2.3995F, 2.336F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up2", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(5.0F, 0.0F, -7.5F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.625F, AnimationHelper.createRotationalVector(5.0047F, -2.4905F, -7.718F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.75F, AnimationHelper.createRotationalVector(2.2693F, -2.9167F, -2.7307F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(7.5149F, -2.7079F, -7.7323F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(2.3906F, -3.0322F, -5.2503F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.125F, AnimationHelper.createRotationalVector(-2.6126F, -0.5344F, -5.1461F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(-2.6334F, -0.4199F, -2.6486F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(2.3666F, -0.4199F, -2.6486F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.625F, AnimationHelper.createRotationalVector(-0.1334F, -0.4199F, -2.6486F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.5833F, AnimationHelper.createRotationalVector(-2.5237F, -7.5107F, 15.0253F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(-0.4777F, -0.4067F, -0.17F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("up3", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(2.4976F, -0.109F, 2.4976F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.625F, AnimationHelper.createRotationalVector(2.5067F, 4.8862F, 2.716F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(4.5706F, 5.086F, -2.299F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(-0.4294F, 5.086F, -2.299F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(-0.2066F, 5.0999F, 0.2109F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(-7.7066F, 5.0999F, 0.2109F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.625F, AnimationHelper.createRotationalVector(-5.2066F, 5.0999F, 0.2109F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(2.2871F, 0.1369F, 1.3171F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow2", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(5.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(2.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(-2.5F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow3", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 7.5F, -7.5F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.75F, AnimationHelper.createRotationalVector(0.9845F, 7.4355F, 0.064F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(8.4845F, 7.4355F, 0.064F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0833F, AnimationHelper.createRotationalVector(3.4845F, 7.4355F, 0.064F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(0.9845F, 7.4355F, 0.064F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.5F, AnimationHelper.createRotationalVector(0.9997F, 12.4347F, 0.1518F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.75F, AnimationHelper.createRotationalVector(-10.1053F, 12.4738F, -4.9686F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(2.1124F, -2.7079F, -0.5874F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow4", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.2187F, -4.9952F, 4.9905F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.625F, AnimationHelper.createRotationalVector(0.6543F, 5.0422F, 0.0857F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(-0.0111F, 5.0844F, -7.4436F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.1667F, AnimationHelper.createRotationalVector(-5.0111F, 5.0844F, -7.4436F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(-2.9453F, 4.8463F, -12.4567F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.625F, AnimationHelper.createRotationalVector(-5.0111F, 5.0844F, -7.4436F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.4167F, AnimationHelper.createRotationalVector(-3.9874F, 3.9752F, 10.2497F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(2.7479F, 3.019F, -2.2372F), Transformation.Interpolations.LINEAR)
            ))
            .addBoneAnimation("bellow1", new Transformation(Transformation.Targets.ROTATE,
                    new Keyframe(0.0F, AnimationHelper.createRotationalVector(0.0F, 0.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.25F, AnimationHelper.createRotationalVector(0.0F, 5.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.625F, AnimationHelper.createRotationalVector(0.4369F, 4.9809F, 5.019F), Transformation.Interpolations.LINEAR),
                    new Keyframe(0.75F, AnimationHelper.createRotationalVector(0.0F, 5.0F, 0.0F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.0417F, AnimationHelper.createRotationalVector(0.4369F, 4.9809F, 5.019F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.125F, AnimationHelper.createRotationalVector(0.2187F, 4.9952F, 2.5095F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.5F, AnimationHelper.createRotationalVector(0.4369F, 4.9809F, 5.019F), Transformation.Interpolations.LINEAR),
                    new Keyframe(1.625F, AnimationHelper.createRotationalVector(-0.2187F, 4.9952F, -2.5095F), Transformation.Interpolations.LINEAR),
                    new Keyframe(3.6667F, AnimationHelper.createRotationalVector(12.2813F, -5.0047F, -2.4714F), Transformation.Interpolations.LINEAR),
                    new Keyframe(4.0F, AnimationHelper.createRotationalVector(1.8356F, -5.1844F, 2.5452F), Transformation.Interpolations.LINEAR)
            ))
            .build();
}
