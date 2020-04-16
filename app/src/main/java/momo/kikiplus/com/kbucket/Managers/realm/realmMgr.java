//package momo.kikiplus.com.kbucket.Managers.realm;
//
//import java.util.ArrayList;
//
//import io.realm.Realm;
//import io.realm.RealmResults;
//import momo.kikiplus.com.kbucket.Utils.KLog;
//import momo.kikiplus.com.kbucket.view.Bean.Bucket;
//import momo.kikiplus.com.kbucket.view.Bean.BucketRank;
//import momo.kikiplus.com.kbucket.view.Bean.Comment;
//import momo.kikiplus.com.kbucket.view.Bean.PostData;
//import momo.kikiplus.com.kbucket.view.Bean.UpdateApp;
//import momo.kikiplus.refactoring.net.Version;
//
//public class realmMgr {
//    private static Realm
//            mRealm;
//    private static String TAG = "realmMgr";
//
//    /**
//     * 생성자
//     */
//    public realmMgr() {
//        try {
//            mRealm = Realm.getDefaultInstance();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 인스턴스 반환 메소드
//     *
//     * @return Realm 인스턴스
//     */
//    public static Realm getInstance() {
//        try {
//            mRealm = Realm.getDefaultInstance();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
//        return mRealm;
//    }
//
//    /**
//     * 버킷리스트 반환 메소드
//     *
//     * @return 버킷리스트
//     */
//    public static RealmResults<PostData> getBucketList() {
//        if (mRealm == null) {
//            return null;
//        }
//        RealmResults<PostData> results = mRealm.where(PostData.class).findAll();
//        return results;
//    }
//
//    /**
//     * 버킷리스트 여러건 추가 메소드
//     */
//    public static void insertPostData(final ArrayList<PostData> postDatas) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.delete(PostData.class);
//                for (int i = 0; i < postDatas.size(); i++) {
//                    PostData postData = realm.createObject(PostData.class, postDatas.get(i).getContents());
//                    postData = postDatas.get(i);
//                }
//            }
//        });
//    }
//
//    /**
//     * 버킷리스트 1건 추가 메소드
//     */
//    public static boolean insertPostData(final PostData postDatas) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                PostData postData = realm.createObject(PostData.class, postDatas.getContents());
//                postData = postDatas;
//            }
//        });
//        return true;
//    }
//
//    /**
//     * 버킷리스트 삭제 메소드
//     */
//    public static void deletePostData(final String Contents) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                try {
//                    RealmResults<PostData> result = realm.where(PostData.class)
//                            .equalTo("m_contents", Contents)
//                            .findAll();
//                    if (result != null) {
//                        result.deleteAllFromRealm();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    KLog.d(TAG, "@@ deletePostData Exception  : " + e.getMessage().toString());
//                }
//            }
//        });
//    }
//
//    /**
//     * 버전 정보 업데이트 메소드
//     */
//    public static void updateVersion(final Version version) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                try {
//                    realm.delete(Version.class);
//                    Version updateVersion = realm.createObject(Version.class, version);
//                    updateVersion = version;
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    KLog.d(TAG, "@@ updateVersion Exception  : " + e.getMessage().toString());
//
//                }
//            }
//        });
//    }
//
//    /**
//     * 버전정보 반환 메소드
//     *
//     * @return 버킷리스트
//     */
//    public static RealmResults<Version> selectVersion() {
//        if (mRealm == null) {
//            return null;
//        }
//        RealmResults<Version> results = mRealm.where(Version.class).findAll();
//        return results;
//    }
//
//    /**
//     * 업데이트 내역 정보 업데이트 메소드
//     */
//    public static void updateNotice(final ArrayList<UpdateApp> updateList) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.delete(UpdateApp.class);
//                for (int i = 0; i < updateList.size(); i++) {
//                    UpdateApp noticeData = realm.createObject(UpdateApp.class, updateList.get(i));
//                    noticeData = updateList.get(i);
//                }
//            }
//        });
//    }
//
//    /**
//     * 공지리스트 반환 메소드
//     *
//     * @return 공지리스트
//     */
//    public static RealmResults<UpdateApp> selectNoticeList() {
//        if (mRealm == null) {
//            return null;
//        }
//        RealmResults<UpdateApp> results = mRealm.where(UpdateApp.class).findAll();
//        return results;
//    }
//
//    /**
//     * 버킷랭킹 정보 업데이트 메소드
//     */
//    public static void updateBucketRanking(final ArrayList<BucketRank> updateList) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.delete(BucketRank.class);
//                for (int i = 0; i < updateList.size(); i++) {
//                    BucketRank rankData = realm.createObject(BucketRank.class, updateList.get(i));
//                    rankData = updateList.get(i);
//                }
//            }
//        });
//
//    }
//
//    /**
//     * 버킷랭킹 정보  반환 메소드
//     *
//     * @return 버킷랭킹 리스트
//     */
//    public static RealmResults<BucketRank> selectBucketRankList() {
//        if (mRealm == null) {
//            return null;
//        }
//        RealmResults<BucketRank> results = mRealm.where(BucketRank.class).findAll();
//        return results;
//    }
//
//    /**
//     * 모두 가지 리스트 업데이트 메소드
//     */
//    public static void updateBucketShare(final ArrayList<Bucket> updateList) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.delete(Bucket.class);
//                for (int i = 0; i < updateList.size(); i++) {
//                    Bucket bucketData = realm.createObject(Bucket.class, updateList.get(i));
//                    bucketData = updateList.get(i);
//                }
//            }
//        });
//    }
//
//    /**
//     * 모두 가지 리스트  반환 메소드
//     *
//     * @return 모두 가지 리스트
//     */
//    public static RealmResults<Bucket> selectBucketShareList() {
//        if (mRealm == null) {
//            return null;
//        }
//        RealmResults<Bucket> results = mRealm.where(Bucket.class).findAll();
//        return results;
//    }
//
//    /**
//     * 모두 가지 코멘트 리스트 업데이트 메소드
//     */
//    public static void updateBucketComment(final ArrayList<Comment> updateList, final int bucketNo) {
//        if (mRealm == null) {
//            mRealm = getInstance();
//        }
//        mRealm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                for (int i = 0; i < updateList.size(); i++) {
//
//                    try {
//                        RealmResults<Comment> result = realm.where(Comment.class)
//                                .equalTo("mBucketNo", bucketNo)
//                                .equalTo("mContent", updateList.get(i).getContent())
//                                .findAll();
//                        //기존에 없으면 insert
//                        if (result == null) {
//                            Comment commentData = realm.createObject(Comment.class, updateList.get(i));
//                            commentData = updateList.get(i);
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        KLog.d(TAG, "@@ deletePostData Exception  : " + e.getMessage().toString());
//                    }
//                }
//            }
//        });
//    }
//
//    /**
//     * 모두 가지 코멘트 리스트  반환 메소드
//     *
//     * @return 모두 가지 코멘트 리스트
//     */
//    public static RealmResults<Comment> selectBucketCommentList(int bucketNo) {
//        if (mRealm == null) {
//            return null;
//        }
//        RealmResults<Comment> results = mRealm.where(Comment.class).equalTo("mIdx", bucketNo).findAll();
//        return results;
//    }
//}
