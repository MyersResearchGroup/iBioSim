package backend.verification.platu.project;
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package platu.project;
//
//import java.util.Set;
//
//import lmoore.TimedStateGraph;
//import platu.lpn.LPN;
//import platu.stategraph.StateGraph;
//
///**
// *
// * @author ldtwo
// */
//public class DesignUnit {
//
//    public LPN lpnModel=null;
//    public LPN sgModel;
//    public Set<DesignUnit> duSet;
//
//    public DesignUnit() {
//    }
//
//    public DesignUnit( Set<DesignUnit> duSet) {
//           this.duSet = duSet;
//    }
//
//    public DesignUnit(LPN lpnModel, LPN sgmodel) {
//        this.lpnModel = lpnModel;
//        this.sgModel = sgmodel;
//    }
//
//    public DesignUnit(LPN lpnModel) {
//        this.lpnModel = lpnModel;
//    }
//
//    /**
//     * @return the lpnmodel
//     */
//    public LPN getLPNModel() {
//        if(lpnModel==null)return reduce();
//        return lpnModel;
//    }
//
// 
//
//    public LPN reduce() {
//        LPN lpn, ret=null;
//        for (DesignUnit du : duSet) {
//            if(ret==null)
//            if (du.lpnModel == null) {
//                ret = du.reduce();
//            } else {
//                ret = du.getLPNModel();
//            }else
//            if (du.lpnModel == null) {
//                lpn = du.reduce();
//                ret.compose(lpn, "tmp-"+System.currentTimeMillis()+".lpn");
//            } else {
//                lpn = du.getLPNModel();
//                ret.compose(lpn, "tmp-"+System.currentTimeMillis()+".lpn");
//            }
//        }
//        return ret;
//    }
//
//    /**
//     * @return the du
//     */
//    public Set<DesignUnit> getDu() {
//        return duSet;
//    }
//
//    /**
//     * @param du the du to set
//     */
//    public void setDu(Set<DesignUnit> du) {
//        this.duSet = du;
//    }
//
//    /**
//     * @return the sgModel
//     */
//    public StateGraph getSGModelUntimed() {
//        return (StateGraph) sgModel;
//    }
//  public TimedStateGraph getSGModel() {
//        return (TimedStateGraph) sgModel;
//    }
//
//    /**
//     * @param sgModel the sgModel to set
//     */
//    public void setSgModel(TimedStateGraph sgModel) {
//        this.sgModel = sgModel;
//    }  /**
//     * @param sgModel the sgModel to set
//     */
//    public void setSgModel(StateGraph sgModel) {
//        this.sgModel = sgModel;
//    }
//}
