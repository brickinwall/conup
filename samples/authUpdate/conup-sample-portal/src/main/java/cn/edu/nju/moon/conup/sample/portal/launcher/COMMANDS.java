package cn.edu.nju.moon.conup.sample.portal.launcher;

/**
 * 
 * @Author JiangWang<jiang.wang88@gmail.com>
 *
 */
public enum COMMANDS {
	/** simply access specified times without update*/
	access,
	/** simply update the component without accessing */
	update,
	/** access the component and execute update the component specified times */
	updateAt,
	/** help of the commands */
	help,
	/** ondemand */
	ondemand,
	/** exit */
	exit;
}
